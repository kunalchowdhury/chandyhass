import com.chandyhass.model.LightweightProcess;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javassist.*;
import javassist.bytecode.BadBytecode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;


public class Test {
    public java.util.Set setvalue = com.google.common.collect.Sets.newHashSet(Arrays.asList("a","b","c")); ;

    public static void main(String[] args) {
        Map<String, Integer> m = Maps.newHashMap();
        m.put("A", 10);
        System.out.println(m);
        m.computeIfPresent("A", (s, v) -> v -1);
        System.out.println(m);
    }
    public static void main4(String[] args) throws NotFoundException, CannotCompileException, IOException, IllegalAccessException, InstantiationException {

        ClassPool cp = ClassPool.getDefault();
        cp.importPackage("java.util");
        cp.importPackage("com.google.common.collect");
        CtClass cc = cp.makeClass("com.chandybass.process.TestRunnable");


        String str = "teststr" ;
        CtField f = CtField.make("public String svalue = "+"\""+str+"\""+";", cc);
        cc.addField(f);

        int i = 100;
        CtField f1 = CtField.make("public int iivalue = "+i+";", cc);
        cc.addField(f1);

        boolean v = true;
        CtField f2 = CtField.make("public boolean bvalue = "+v+";", cc);
        cc.addField(f2);

        String a1 = "a" ;
        String b1 = "b" ;
        String c1 = "c" ;

        StringBuilder sb = new StringBuilder("Sets.newHashSet(Arrays.asList(");
        sb.append("\"").append(a1).append("\",");
        sb.append("\"").append(b1).append("\",");
        sb.append("\"").append(c1).append("\"");
        sb.append("));");
        String set = sb.toString();
        String src = "public Set setvalue = new HashSet();";
        CtField f3 = CtField.make(src, cc);
        cc.addField(f3);


        cc.setInterfaces(new CtClass[]{cp.get("java.lang.Runnable")});
        String run = "public void run(){while(true){System.out.println(\"Sleeping..\"); Thread.sleep(1000L);}}" ;
        CtMethod method = CtNewMethod.make(run, cc);
        CtClass etype = ClassPool.getDefault().get("java.io.IOException");
        method.addCatch("{ System.out.println($e); throw $e;}", etype) ;
        cc.addMethod(method);
        new Thread((Runnable) cc.toClass().newInstance()).start();
       //  cc.writeFile("src/main/java");
    }
    public static void main3(String[] args) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
      //  ProcessBuilder processBuilder = new ProcessBuilder()
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("com.chandyhass.model.LighweightProcess");
        CtMethod m = cc.getDeclaredMethod("run");
        m.insertBefore("{ System.out.println(\"LighweightProcess.say():\"); }");
        Class c = cc.toClass();
        LightweightProcess h = (LightweightProcess)c.newInstance();

    }
    public static void main2(String[] args) throws NotFoundException, CannotCompileException, IOException, BadBytecode {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get("Point");
        CtMethod m = cc.getDeclaredMethod("move");
        m.getMethodInfo().rebuildStackMap(pool);

        m.insertBefore("{ System.out.println($1); System.out.println($2); }");
        cc.writeFile();
    }
    public static void main1(String[] args) throws IOException, CannotCompileException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get("Rectangle");
        cc.setSuperclass(pool.get("Point"));
        cc.writeFile();

    }
}
