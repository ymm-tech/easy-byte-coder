package io.manbang.easybytecoder.plugin.simplehotfix.mock.runtime;


import io.manbang.helloworld.HelloWorld.UserModel;

/**
 * @author xujie
 */
public class FixHandle {
    public static UserModel fixModel(UserModel userModel) {
        if (userModel == null) {
            UserModel newUserModel = new UserModel();
            newUserModel.setName("zhangsan");
            newUserModel.setAge(18);
            return newUserModel;
        }
        return userModel;
    }

    public static void test() {
        System.out.println("111");
    }
}
