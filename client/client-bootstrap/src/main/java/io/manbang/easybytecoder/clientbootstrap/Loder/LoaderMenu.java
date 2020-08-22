package io.manbang.easybytecoder.clientbootstrap.Loder;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.manbang.easybytecoder.clientbootstrap.Loder.gui.ViewMenu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author xujie
 */
public class LoaderMenu {
    public void displayMenu() {
        printSpacing(2);
        printSelectPid();
    }


    private void printSpacing(int line) {
        for (int i = 0; i < line; i++) {
            System.out.println("\n");
        }

    }


    private void printSelectPid() {
        HashMap<String, String> processNameAndPid = getProcessNameAndPid();
        HashMap<String, String> newProcessNameAndPid = new HashMap<>(10);
        for (String processName : processNameAndPid.keySet()) {
            String[] name = processName.split(" ");
            newProcessNameAndPid.put(name[0], processNameAndPid.get(processName));
        }
        try {
            new ViewMenu().SelectPid(newProcessNameAndPid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private HashMap<String, String> getProcessNameAndPid() {
        HashMap<String, String> ProcessNamePidMapping = new HashMap<>(10);
        for (VirtualMachineDescriptor jvm : VirtualMachine.list()) {
            ProcessNamePidMapping.put(jvm.displayName(), jvm.id());
        }
        return ProcessNamePidMapping;
    }


    private void ScannerKeyboard() {
        Scanner sc = new Scanner(System.in);
        System.out.println("ScannerTest, Please Enter Name:");
        String name = sc.nextLine();  //读取字符串型输入
        System.out.println("ScannerTest, Please Enter Age:");
        int age = sc.nextInt();    //读取整型输入
        System.out.println("ScannerTest, Please Enter Salary:");
        float salary = sc.nextFloat(); //读取float型输入
        System.out.println("Your Information is as below:");
        System.out.println("Name:" + name + "\n" + "Age:" + age + "\n" + "Salary:" + salary);
    }

}
