package lxfIoC.framework;

import lxfIoC.framework.inject.InjectContext;

public class Test {

    public static void main(String[] args) throws Exception {

//        Teacher teacher = new Teacher();
//        teacher.drink();

        InjectContext injectContext = new InjectContext("lxfIoC.framework");
        Object water = injectContext.getBean("water");


        Object water1 = injectContext.getBean("water");

        System.out.println("aaa");
    }
}
