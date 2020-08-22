package io.manbang.easybytecoder.traffichandler;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TtlTransformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(com.alibaba.ttl.threadpool.agent.TtlTransformer.class);

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private final List<JavassistTransformlet> transformletList = new ArrayList<JavassistTransformlet>();

    TtlTransformer(List<? extends JavassistTransformlet> transformletList) {
        for (JavassistTransformlet transformlet : transformletList) {
            this.transformletList.add(transformlet);
            logger.info("[TtlTransformer] add Transformlet " + transformlet.getClass() + " success");
        }
    }

    @Override
    public final byte[] transform( final ClassLoader loader, final String classFile, final Class<?> classBeingRedefined,
                                  final ProtectionDomain protectionDomain, final byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) {
                return classFileBuffer;
            }

            final String className = toClassName(classFile);
            for (JavassistTransformlet transformlet : transformletList) {
                final byte[] bytes = transformlet.doTransform(className, classFileBuffer, loader);
                if (bytes != null) {
                    return bytes;
                }
            }
        } catch (Throwable t) {
            String msg = "Fail to transform class " + classFile + ", cause: " + t.toString();
            logger.log(Level.SEVERE, msg, t);
            throw new IllegalStateException(msg, t);
        }

        return classFileBuffer;
    }

    private static String toClassName(final String classFile) {
        return classFile.replace('/', '.');
    }

}
