package io.manbang.helloworld;

/**
 * @author xujie
 */

public class HelloWorld {
    public static void main(String[] args) throws InterruptedException {
        new HelloWorld().loop(null);

    }


    void loop(UserModel userModel) throws InterruptedException {
        while (true) {
            runPrint(userModel);
        }
    }

    void runPrint(UserModel userModel) throws InterruptedException {
        try {
            System.out.println(userModel.getName());
        } catch (Throwable cause) {
            cause.printStackTrace();
        }
        Thread.sleep(1000);
    }

    public static class UserModel {
        String name;
        int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}


