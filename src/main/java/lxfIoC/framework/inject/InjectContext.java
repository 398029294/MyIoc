package lxfIoC.framework.inject;

import lxfIoC.framework.annotion.Autowired;
import lxfIoC.framework.annotion.Component;
import lxfIoC.framework.exception.UnexpectedBeanDefitionalException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class InjectContext {
    //存放接口对应的实体子类集合
    public static HashMap<Class , ArrayList<Class>> fatherAndSonClass = new HashMap<>();

    //存放被component管理的类
    public  ArrayList<Class> beanList = new ArrayList();

    //存放beans的地方 根据id获取实体类
    public  static HashMap<String ,Object> beanMap = new HashMap<>();

    private String basePackage;

    /*
        构造方法 初始化 beansList 和 comList
     */

    public InjectContext() { }

    public InjectContext(String basePackage) throws Exception {
        this.basePackage = basePackage;
        init(basePackage);
    }

    private void init(String basePackage) throws Exception {
        String path = InjectContext.class.getResource("/").getPath();
        String s = basePackage.replaceAll("\\.", "/");
        path = path + s;//设置扫描范围

        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files) {
            //查出.class结尾的文件
            if(null != f && f.getName().endsWith(".class")){

                Class<?> aClass = Class.forName(basePackage + "." +f.getName().split("\\.")[0]);

                //遍历所有类
                Component annotation = aClass.getAnnotation(Component.class);
                //查出被component管理的类
                if(null != annotation){
                    //加入comList中
                    beanList.add(aClass);

                    //查看此被标记的类是否有父层接口
                    Class<?>[] interfaces = aClass.getInterfaces();

                    for (Class<?> anInterface : interfaces) {
                            //根据此接口 在beanMap中找到他的所有字类，如果第一次 那就创建

                        ArrayList<Class> classes = fatherAndSonClass.get(anInterface);

                        if(null == classes){
                            //第一次
                            ArrayList<Class> son = new ArrayList<>();
                            son.add(aClass);
                            fatherAndSonClass.put(anInterface,son);
                        }else {
                            classes.add(aClass);
                        }

                    }
                }



            }

        }
        System.out.println(fatherAndSonClass);

        //开始 根据beanList 通过id开始注入
        for (Class aClass : beanList) {
            String id = getId(aClass);

            //注入
            Object inject = inject(aClass);

            //最后根据id获取bean
            beanMap.put(id,inject);

        }

    }
    /*
        获取唯一id
     */
    private String getId(Class aClass){
        String name = aClass.getName();
        String[] split = name.split("\\.");
        String id= split[split.length - 1].substring(0,1).toLowerCase() + split[split.length - 1].substring(1);
        return id;
    }

    private Object inject(Class leadClass) throws Exception {

        //获取当前类中所有属性
        Field[] declaredFields = leadClass.getDeclaredFields();

        //保证唯一性
        Object bean = beanMap.get(leadClass);

        //获取当前类id
        String id = getId(leadClass);
        if(bean == null){
            //如果没有则创建一个
            bean = leadClass.newInstance();
            beanMap.put(id,bean);

        }

        for (Field declaredField : declaredFields) {
            //设置所有属性都可被反射
            declaredField.setAccessible(true);

            //查看属性是否有@Auwired注解
            Autowired annotation = declaredField.getAnnotation(Autowired.class);
            if(null != annotation){

                Class<?> type = declaredField.getType();
                //判断当前属性是否为接口
                if(type.isInterface()){
                    //类是个接口 则不能直接创建实例 不然会报错 获取他的所有子类
                    ArrayList<Class> classes = fatherAndSonClass.get(type);
                        //常理一般不会为null 如果为null则报错
                    if(null == classes){
                        throw new NullPointerException();
                    }

                    //如果当前接口只有一个实现字类
                    if(classes.size() == 1){
                        //解决单例问题
                        Class aClass = classes.get(0);
                        String id1 = getId(aClass);

                        Object o = beanMap.get(id1);
                        if(null == o){
                            //第一次创建
                            Object instance = aClass.newInstance();
                            declaredField.set(bean,instance);
                            beanMap.put(id1,instance);
                        }else {
                            //不是第一次创建
                            declaredField.set(bean,o);
                        }

                    }else {

                    //如果当前接口有多个实现类 但是属性name都不匹配
                        boolean isAutowiredFail = true;

                        for (Class aClass : classes) {
                            String className = aClass.getName();

                            String fileName = declaredField.getName();
                            String first = fileName.substring(0, 1).toUpperCase();

                            fileName = first + fileName.substring(1);

                            if(aClass.getName().endsWith(fileName)){

                                String id1 = getId(aClass);
                                Object o = beanMap.get(id1);

                                if(null == o){
                                    Object instance = aClass.newInstance();
                                    declaredField.set(bean,instance);
                                    beanMap.put(id1,instance);

                                }else {
                                    declaredField.set(bean,o);
                                }

                                isAutowiredFail = false;
                            }
                            System.out.println(fileName);


                        }

                        if(isAutowiredFail){
                            throw new UnexpectedBeanDefitionalException("原本期望找到1个对象注入，但是了找到了2个，无法识别应该注入哪一个");
                        }
                    }
                }else {
                    //当前属性不是接口
                    //解决单例问题
                    String id1 = getId(type);
                    Object o = beanMap.get(id1);
                    if(null == o ){
                        //第一次创建
                        Object instance = type.newInstance();
                        declaredField.set(bean,instance);
                        beanMap.put(id1,instance);
                    }else {
                        //不是第一次创建
                        declaredField.set(bean,o);
                    }

                }
            }

        }

        return  bean;
    }

    public Object getBean(String id){
        return beanMap.get(id);
    }
}
