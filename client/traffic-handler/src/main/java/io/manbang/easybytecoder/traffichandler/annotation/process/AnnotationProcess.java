package io.manbang.easybytecoder.traffichandler.annotation.process;

import com.google.common.collect.Lists;
import io.manbang.easybytecoder.traffichandler.AttachTrafficHandler;
import io.manbang.easybytecoder.traffichandler.BaseTransformer;
import io.manbang.easybytecoder.traffichandler.TrafficHandler;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyClassName;
import io.manbang.easybytecoder.traffichandler.annotation.ModifyMethod;
import io.manbang.easybytecoder.traffichandler.annotation.ResourceToImport;
import io.manbang.easybytecoder.traffichandler.annotation.constant.CodePatternEnum;
import io.manbang.easybytecoder.traffichandler.annotation.model.ModifyMethodModel;
import io.manbang.easybytecoder.traffichandler.modifier.CatchCode;
import io.manbang.easybytecoder.traffichandler.modifier.ClassModifier;
import io.manbang.easybytecoder.traffichandler.modifier.MethodModifyCode;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationProcess {

    public BaseTransformer baseTransformer;

    public List<String> classNameList;


    public List<String> getClassNameList() {
        return classNameList;
    }

    public BaseTransformer getBaseTransformer() {
        return baseTransformer;
    }


    public void process(AttachTrafficHandler attachTrafficHandler, final String jarFilePath) throws InvocationTargetException, IllegalAccessException {
        baseHandler(attachTrafficHandler, jarFilePath);

    }

    public void baseHandler(Object handler, final String jarFilePath) throws IllegalAccessException, InvocationTargetException {
        ModifyClassName modifyClassNameAnnotation = handler.getClass().getAnnotation(ModifyClassName.class);

        String className = null;
        if (modifyClassNameAnnotation != null) {
            className = modifyClassNameAnnotation.value();
        }


        String[] importClass = null;
        ResourceToImport resourceToImportAnnotation = handler.getClass().getAnnotation(ResourceToImport.class);
        if (resourceToImportAnnotation != null) {
            importClass = resourceToImportAnnotation.value();
        }


        final String[] finalImportClass = importClass;
        final String finalClassName = className;


        Method[] declaredMethods = handler.getClass().getDeclaredMethods();
        final HashMap<String, ModifyMethodModel> methodMap = new HashMap<>();
        for (Method declaredMethod : declaredMethods) {
            declaredMethod.setAccessible(true);
            boolean annotationPresent = declaredMethod.isAnnotationPresent(ModifyMethod.class);
            if (annotationPresent) {
                ModifyMethod annotation = declaredMethod.getAnnotation(ModifyMethod.class);
                modifyStrategy(handler, methodMap, declaredMethod, annotation);
            }

        }


        HashMap<String, MethodModifyCode> methodModifiers = new HashMap<>();

        for (final String methodName : methodMap.keySet()) {

            MethodModifyCode methodModifier = new MethodModifyCode() {
                @Override
                public String getToModifyMethodName() {
                    return methodName;
                }

                @Override
                public CtClass[] getToModifyMethodParamDecl(ClassPool pool) {
                    if (methodMap.get(methodName).getMethodParamDecl() != null && methodMap.get(methodName).getMethodParamDecl().length > 0) {
                        String[] paramDecl = methodMap.get(methodName).getMethodParamDecl();
                        int size = paramDecl.length;
                        CtClass[] paramList = new CtClass[size];
                        for (int i = 0; i < paramDecl.length; i++) {
                            try {
                                paramList[i] = pool.get(paramDecl[i]);
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        return paramList;
                    }
                    return new CtClass[0];
                }

                @Override
                public List<String> getCodeInsertBefore() {
                    return methodMap.get(methodName).getBeforeCode();
                }

                @Override
                public List<String> getCodeInsertAfter() {
                    return methodMap.get(methodName).getAfterCode();
                }

                @Override
                public boolean ifNeedCheckParam() {
                    return methodMap.get(methodName).isCheckParam();
                }

                @Override
                public CatchCode getCatchCode() {
                    if (methodMap.get(methodName).getCatchCode() != null) {
                        return methodMap.get(methodName).getCatchCode();
                    }
                    return super.getCatchCode();
                }

                @Override
                public String getCodeToSetBody(CtMethod method) {
                    if (methodMap.get(methodName).getBodyCode() != null) {
                        return methodMap.get(methodName).getBodyCode();
                    }
                    return super.getCodeToSetBody(method);
                }

                @Override
                public String getCodeToSetBody(CtConstructor constructor) {
                    return super.getCodeToSetBody(constructor);
                }

                @Override
                public Map<String, CtClass> localVariables() {
                    if (methodMap.get(methodName).getLocalVariablesCode() != null) {
                        return methodMap.get(methodName).getLocalVariablesCode();
                    }
                    return super.localVariables();
                }
            };


            methodModifiers.put(methodName, methodModifier);
        }


        final HashMap<String, MethodModifyCode> finalMethodModifiers = methodModifiers;
        final ClassModifier classModifier = new ClassModifier() {
            @Override
            public String getToModifyClassName() {
                return finalClassName;
            }

            @Override
            public List<String> getResourceToImport(ClassLoader originLoader) {
                return Lists.newArrayList(finalImportClass);
            }

            @Override
            public MethodModifyCode getMethodModifier(String methodDesc) {
                return finalMethodModifiers.get(methodDesc);
            }
        };


        BaseTransformer baseTransformer = new BaseTransformer() {
            @Override
            public boolean init() {
                classModifiers.put(classModifier.getToModifyClassName(), classModifier);
                return true;
            }

            @Override
            public String getRelatedJarFilePath() {
                return jarFilePath;
            }

            @Override
            public void setRelatedJarFilePath(String jarFilePath) {
            }
        };


        baseTransformer.init();
        this.classNameList = baseTransformer.getClassNameList();
        this.baseTransformer = baseTransformer;
    }

    public void modifyStrategy(Object trafficHandler, HashMap<String, ModifyMethodModel> methodMap, Method declaredMethod, ModifyMethod annotation) throws IllegalAccessException, InvocationTargetException {
        if (methodMap.get(annotation.methodName()) == null) {
            ModifyMethodModel modifyMethodModel = new ModifyMethodModel();

            modifyMethodModel.setCheckParam(declaredMethod.getAnnotation(ModifyMethod.class).checkParam());

            String[] paramDecl = declaredMethod.getAnnotation(ModifyMethod.class).paramDecl();

            if (paramDecl.length > 0) {
                modifyMethodModel.setMethodParamDecl(paramDecl);
            }

            if (annotation.pattern() == CodePatternEnum.Before) {
                String results = (String) declaredMethod.invoke(trafficHandler);
                modifyMethodModel.setBeforeCode(Lists.newArrayList(results));
            }
            if (annotation.pattern() == CodePatternEnum.After) {
                String results = (String) declaredMethod.invoke(trafficHandler);
                modifyMethodModel.setAfterCode(Lists.newArrayList(results));
            }
            if (annotation.pattern() == CodePatternEnum.Body) {
                String results = (String) declaredMethod.invoke(trafficHandler);
                modifyMethodModel.setBodyCode(results);
            }
            if (annotation.pattern() == CodePatternEnum.Catch) {
                CatchCode results = (CatchCode) declaredMethod.invoke(trafficHandler);
                modifyMethodModel.setCatchCode(results);
            }
            if (annotation.pattern() == CodePatternEnum.LocalVariables) {
                Map<String, CtClass> results = (Map<String, CtClass>) declaredMethod.invoke(trafficHandler);
                modifyMethodModel.setLocalVariablesCode(results);
            }

            methodMap.put(annotation.methodName(), modifyMethodModel);
        } else {
            ModifyMethodModel modifyMethodModel = methodMap.get(annotation.methodName());
            if (annotation.pattern() == CodePatternEnum.Before) {
                String results = (String) declaredMethod.invoke(trafficHandler);
                if(modifyMethodModel.getBeforeCode()==null){
                    modifyMethodModel.setBeforeCode(Lists.newArrayList(results));
                }
                modifyMethodModel.getBeforeCode().add(results);
            }
            if (annotation.pattern() == CodePatternEnum.After) {
                String results = (String) declaredMethod.invoke(trafficHandler);
                if(modifyMethodModel.getAfterCode()==null){
                    modifyMethodModel.setAfterCode(Lists.newArrayList(results));
                }
                modifyMethodModel.getAfterCode().add(results);
            }
            if (annotation.pattern() == CodePatternEnum.LocalVariables) {
                Map<String, CtClass> results = (Map<String, CtClass>) declaredMethod.invoke(trafficHandler);
                if(modifyMethodModel.getLocalVariablesCode()==null){
                    modifyMethodModel.setLocalVariablesCode(results);
                }
                modifyMethodModel.getLocalVariablesCode().putAll(results);
            }
        }


    }


    public void process(TrafficHandler trafficHandler, final String jarFilePath) throws InvocationTargetException, IllegalAccessException {
        baseHandler(trafficHandler, jarFilePath);
    }
}
