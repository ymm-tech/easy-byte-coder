package io.manbang.easybytecoder.traffichandler;

import io.manbang.easybytecoder.traffichandler.modifier.CatchCode;
import io.manbang.easybytecoder.traffichandler.modifier.ClassModifier;
import io.manbang.easybytecoder.traffichandler.modifier.MethodModifyCode;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author xujie
 * 修改类字节码的具体执行方法
 */

public enum DoTransformer {
    /**
     * 单例的DoTransformer实例
     */
    INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(DoTransformer.class);
    private String debugDump = "./dump";

    /**
     * 修改类方法
     *
     * @param className
     * @param modifier
     * @param cp
     * @param ctClass
     * @return
     */
    public byte[] execute(String className, ClassModifier modifier, ClassPool cp, CtClass ctClass) throws Exception {
        try {
            //        enbale when need debug
            CtClass.debugDump = debugDump;
            List<CtField> fieldsToAdd = modifier.getFieldsToAdd(ctClass, cp);
            if (null != fieldsToAdd) {
                for (CtField f : fieldsToAdd) {
                    ctClass.addField(f);
                }
            }
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            CtConstructor[] constructors = ctClass.getConstructors();
            //支持构造函数修改
            constructorsModifier(modifier, ctClass, constructors);
            //方法级别的修改
            for (CtMethod method : declaredMethods) {
                MethodModifyCode methodModifier = modifier.getMethodModifier(method.getName());
                if (methodModifier == null) {
                    logger.warn("skip method because methodModifier is null method:{}", method.getName());
                    continue;
                }
                // 如果需要进行参数匹配，则进行参数匹配
                if (methodModifier.ifNeedCheckParam() && checkParam(cp, method, methodModifier)) {
                    continue;
                }
                //对body的修改
                if (bodyModifier(ctClass, method, methodModifier)) {
                    continue;
                }
                //装载局部变量
                localVariablesModifier(method, methodModifier);
                //在方法运行之前插入相关代码
                beforeModifier(method, methodModifier);
                //在方法运行之后之前插入相关代码
                afterModifier(method, methodModifier);
                //捕获被修改方法的异常
                tryCatchEncircleModifier(method, methodModifier);
                logger.info("find transform method end [{}#{}({})] .", className, method.getName(), assembleMethodParams(method.getParameterTypes()));
            }

        } catch (Throwable e) {
            logger.error("doTransform  className:{} err:", className, e);

        }

        return ctClass.toBytecode();
    }

    /**
     * 对方法使用try Catch包围
     *
     * @param method
     * @param methodModifier
     */
    private void tryCatchEncircleModifier(CtMethod method, MethodModifyCode methodModifier) {
        CatchCode catchCode = methodModifier.getCatchCode();
        if (catchCode != null) {
            try {
                CtClass retype = catchCode.getCatchType();
                if (retype == null) {
                    retype = ClassPool.getDefault().get("java.lang.Exception");
                }
                method.addCatch(catchCode.getCodes(), retype);
            } catch (Exception e) {
                logger.error("addCatch ERROR:{}, code:{}", e, catchCode);
            }
        }
    }

    /**
     * 在原代码方法之后加入代码
     *
     * @param method
     * @param methodModifier
     */
    private void afterModifier(CtMethod method, MethodModifyCode methodModifier) {
        if (methodModifier.getCodeInsertAfter() == null) {
            return;
        }
        for (String afterCode : methodModifier.getCodeInsertAfter()) {
            try {
                method.insertAfter(afterCode);
            } catch (Exception e) {
                logger.error("afterCode ERROR: {}, code:{}", e, afterCode);
            }
        }
    }

    /**
     * 在原代码方法之前加入代码
     *
     * @param method
     * @param methodModifier
     */
    private void beforeModifier(CtMethod method, MethodModifyCode methodModifier) {
        if (methodModifier.getCodeInsertBefore() == null) {
            return;
        }
        List<String> codeToInsertBefore = methodModifier.getCodeInsertBefore();
        Collections.reverse(codeToInsertBefore);
        for (String beforeCode : codeToInsertBefore) {
            try {
                method.insertBefore(beforeCode);
            } catch (Exception e) {
                logger.error("insertBefore ERROR: {}, code:{}", e, beforeCode);
            }
        }
    }

    /**
     * 在方法中加入局部变量
     *
     * @param method
     * @param methodModifier
     */
    private void localVariablesModifier(CtMethod method, MethodModifyCode methodModifier) {
        if (methodModifier.localVariables() == null || methodModifier.localVariables().size() == 0) {
            return;
        }
        for (Map.Entry<String, CtClass> stringCtClassEntry : methodModifier.localVariables().entrySet()) {
            try {
                method.addLocalVariable(stringCtClassEntry.getKey(), stringCtClassEntry.getValue());
            } catch (Exception e) {
                logger.error("addLocalVariable   key:{}  value:{}", stringCtClassEntry.getKey(), stringCtClassEntry.getValue(), e);
            }
        }

    }

    /**
     * 替换整个方法体
     *
     * @param ctClass
     * @param method
     * @param methodModifier
     * @return
     * @throws NotFoundException
     */
    private boolean bodyModifier(CtClass ctClass, CtMethod method, MethodModifyCode methodModifier) throws NotFoundException {
        if (!methodModifier.getCodeToSetBody(method).isEmpty()) {
            logger.info("find transform method body [{}#{}({})].", ctClass.getName(), method.getName(), assembleMethodParams(method.getParameterTypes()));
            try {
                method.setBody(methodModifier.getCodeToSetBody(method));
            } catch (Exception e) {
                logger.error("setBody ERROR:{}, code:{}", e, methodModifier.getCodeToSetBody(method));
            }
            if (!method.getName().equals("registerClients")) {
                insertTryCode(method);
            }
            return true;
        }
        return false;
    }

    /**
     * 检查参数映射，找到符合入参定义的方法
     *
     * @param cp
     * @param method
     * @param methodModifier
     * @return
     * @throws NotFoundException
     */
    private boolean checkParam(ClassPool cp, CtMethod method, MethodModifyCode methodModifier) throws NotFoundException {
        if (methodModifier.ifNeedCheckParamByOwn() && !methodModifier.checkParamByOwn(method.getParameterTypes(), cp)) {
            return true;
        } else {
            CtClass[] paramsNeed = methodModifier.getToModifyMethodParamDecl(cp);
            if (!Objects.deepEquals(paramsNeed, method.getParameterTypes())) {
                logger.trace("skip method for params diff: {}", method.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * 对构造方法的修改
     *
     * @param modifier
     * @param ctClass
     * @param constructors
     * @throws NotFoundException
     */
    private void constructorsModifier(ClassModifier modifier, CtClass ctClass, CtConstructor[] constructors) throws NotFoundException {
        for (CtConstructor ctConstructor : constructors) {
            MethodModifyCode methodModifier = modifier.getMethodModifier(ctConstructor.getName());
            //如果修改的是构造函数，
            if (methodModifier != null) {
                logger.info("find transform constructor body [{}#{}({})].", ctClass.getName(), ctConstructor.getName(), assembleMethodParams(ctConstructor.getParameterTypes()));
                try {
                    ctConstructor.setBody(methodModifier.getCodeToSetBody(ctConstructor));
                } catch (Exception e) {
                    logger.error("setBody ERROR:{}, code:{}", e, methodModifier.getCodeToSetBody(ctConstructor));
                }
                insertTryCode(ctConstructor);
            }
        }
    }


    /**
     * 组装方法的参数
     *
     * @param params
     * @return param1, param2, param3
     */
    private String assembleMethodParams(CtClass[] params) {
        if (params == null) {
            return "";
        }
        List<String> ps = new ArrayList<>(params.length);
        for (CtClass p : params) {
            ps.add(p.getName());
        }
        return org.apache.commons.lang3.StringUtils.join(ps, ",");
    }


    /**
     * 增加默认的tryCatc
     *
     * @param method
     */
    private void insertTryCode(CtBehavior method) {
        if (method.getMethodInfo().getCodeAttribute() == null) {
            return;
        }
        try {
            CtClass etype = ClassPool.getDefault().get("java.lang.Exception");
            method.addCatch("{ EasyByteCoderResourceObjectPool.getEasyByteCoderLogger().error(\"invoke method Failed!, error:\",$e); throw $e; }", etype);
        } catch (Exception e) {
            logger.error("insertTryCode error:", e);
        }

    }
}
