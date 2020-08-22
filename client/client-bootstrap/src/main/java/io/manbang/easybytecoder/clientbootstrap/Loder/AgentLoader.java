package io.manbang.easybytecoder.clientbootstrap.Loder;


import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;

/**
 * @author xujie
 */
public class AgentLoader {

    public static void run(String bootstrapJarPath) {


        new LoaderMenu().displayMenu();
    }

    public static void start(String pid){
        if (VirtualMachine.list() == null) {
            System.out.println("err VirtualMachine list==null");
            return;
        }

        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor jvm : VirtualMachine.list()) {
            if (jvm.id().equals(pid)) {
                virtualMachineDescriptor = jvm;
                break;
            }
        }

        if (virtualMachineDescriptor == null) {
            System.out.println("Target Application not found");
            return;
        }

        String path=System.getProperty("user.dir");
        String jarName=System.getProperty("java.class.path");
        String bootPath=path+"/"+jarName;
        File agentFile = new File(bootPath);
        try {
            System.out.println("Attaching to target JVM with PID: " +pid);
            VirtualMachine jvm = VirtualMachine.attach(pid);
            jvm.loadAgent(agentFile.getAbsolutePath(), bootPath);
            jvm.detach();
            System.out.println("Attached to target JVM and loaded Java agent successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
