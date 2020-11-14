package com.chandybass.kernel.process;

import com.chandyhass.model.LightweightProcess;
import javassist.*;

import java.io.IOException;
import java.util.Set;

public class LightweightProcessBuilder {
    private final String clsName;
    public CtClass cc ;
    private ClassPool cp ;

    public LightweightProcessBuilder(String pid) throws Exception {
        this.cp = ClassPool.getDefault();
        cp.importPackage("java.util");
        cp.importPackage("java.util.concurrent");
        cp.importPackage("com.google.common.collect");
        cp.importPackage("com.chandyhass.server");
        cp.importPackage("com.chandyhass.process");
        this.cc = cp.makeClass("com.chandyhass.process.Process"+pid);
        this.clsName = "Process"+pid;
        this.cc.setInterfaces(new CtClass[]{cp.get("com.chandyhass.model.LightweightProcess")});
        String run = "public void run(){while(true){System.out.println(\"Sleeping..\"); Thread.sleep(1000L);}}" ;
        CtMethod method = CtNewMethod.make(run, cc);
        CtClass etype = ClassPool.getDefault().get("java.io.IOException");
        method.addCatch("{ System.out.println($e); throw $e;}", etype) ;
        cc.addMethod(method);
    }
    public LightweightProcessBuilder addPid(String pid) throws CannotCompileException {
        CtField f = CtField.make("public String pid = "+"\""+pid+"\""+";", cc);
        cc.addField(f);
        String meth = "public String pid(){return pid;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        return this;
    }

    public LightweightProcessBuilder addAdminPort(String adminPort) throws CannotCompileException {
        CtField f = CtField.make("public String adminPort = "+"\""+adminPort+"\""+";", cc);
        cc.addField(f);
        String meth = "public int getAdminPort(){return Integer.parseInt(adminPort);}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        return this;
    }

    public LightweightProcessBuilder addDataPort(String dataPort) throws CannotCompileException {
        CtField f = CtField.make("public String dataPort = "+"\""+dataPort+"\""+";", cc);
        cc.addField(f);
        String meth = "public int getDataPort(){return Integer.parseInt(dataPort);}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        return this;
    }

    public LightweightProcessBuilder addHostname(String hostName) throws CannotCompileException {
        CtField f = CtField.make("public String hostName = "+"\""+hostName+"\""+";", cc);
        cc.addField(f);
        String meth = "public String getHostName(){return hostName;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        return this;
    }

    public LightweightProcessBuilder addBlockingParam(String blocking) throws CannotCompileException {
        CtField f = CtField.make("public boolean blocking = "+Boolean.valueOf(blocking)+";", cc);
        cc.addField(f);
        String meth = "public boolean blocking(){return blocking;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        return this;
    }

    public LightweightProcessBuilder addDependentProcesses(Set<String> strings) throws CannotCompileException {
        CtField f = CtField.make("public Set dependentProcess = new HashSet(); ", cc);
        cc.addField(f);
        String[] meth = {"public Set getDependentProcesses(){"};
        strings.forEach(s -> {meth[0] = meth[0].concat("dependentProcess.add(\""+s+"\");");});
        meth[0]+= "return dependentProcess;}" ;
        CtMethod method = CtNewMethod.make(meth[0], cc);
        cc.addMethod(method);
        return this;
    }

    public void addAdminServer() throws CannotCompileException {
        CtField f = CtField.make("public SimpleNonBlockingServer adminServer =" +
                " new SimpleNonBlockingServer(getHostName(), getAdminPort(), this) ;", cc);
        cc.addField(f);
        String meth = "public SimpleNonBlockingServer getAdminServer(){return adminServer;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
    }

    public void addDataServer() throws CannotCompileException {
        CtField f = CtField.make("public SimpleNonBlockingServer dataServer =" +
                " new SimpleNonBlockingServer(getHostName(), getDataPort(), this) ;", cc);
        cc.addField(f);
        String meth = "public SimpleNonBlockingServer getDataServer(){return dataServer;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
    }

    public LightweightProcess build() throws CannotCompileException, IOException, IllegalAccessException,
            InstantiationException {
        addAdminServer();
        addDataServer();

        CtField f = CtField.make("public Map numMap =" +
                " new ConcurrentHashMap() ;", cc);
        cc.addField(f);
        String meth = "public Map getNumMap(){return numMap;}" ;
        CtMethod method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);

        f = CtField.make("public Map waitMap =" +
                " new ConcurrentHashMap() ;", cc);
        cc.addField(f);
        meth = "public Map getWaitMap(){return waitMap;}" ;
        method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);

        f = CtField.make("public boolean initiator =" +
                " false ;", cc);
        cc.addField(f);
        meth = "public boolean isInitiator(){return initiator;}" ;
        method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);

        f = CtField.make("private String engagingQueryPID =" +
                " null ;", cc);
        cc.addField(f);
        meth = "public String getEngagingQueryPID(){return engagingQueryPID;}" ;
        method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);

        meth = "public void setEngagingQueryPID(String pid){ this.engagingQueryPID = pid;}" ;
        method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);

        cc.debugWriteFile("src/main/java");


        meth = "public static void main(String args[]){ new "+this.clsName+"().start();} " ;
        method = CtNewMethod.make(meth, cc);
        cc.addMethod(method);
        cc.debugWriteFile("src/main/java");
        return (LightweightProcess) cc.toClass().newInstance();

    }

}
