package com.chandybass.kernel;

import com.chandybass.kernel.process.LightweightProcessBuilder;
import com.chandyhass.model.LightweightProcess;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Orchestrator {
    private final static Logger logger = LoggerFactory.getLogger(Orchestrator.class);

    private final static String PROPERTIES_FILE_PATH = "config.properties";
    private final static Collection<String> PROP_KEYS = Arrays.asList(
            "process.ids", "process.admin.port", "process.data.port",
            "process.host.name", "process.blocking", "process.dependency");

    private final Collection<Process> processes  = Sets.newHashSet();
    private static final Map<String, Integer> adminPortMap = Maps.newHashMap();
    private static final Map<Integer, String> idxPidMap = Maps.newHashMap();
    private static final Map<String, String> localhostMap = Maps.newHashMap();
    public void init() {
        Properties properties = new Properties();
        try (InputStream inputStream =
                     Orchestrator.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH)) {
            properties.load(inputStream);
            ArrayList<LightweightProcessBuilder> processBuilders = Lists.newArrayList();
            Map<Integer, Set<String>> dependencySet = Maps.newHashMap();
            Collection<LightweightProcess> lwProcesses = Sets.newHashSet();
            PROP_KEYS.forEach(prop -> {
                switch (prop) {
                    case "process.ids":
                        MutableInt id = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            LightweightProcessBuilder pb;
                            try {
                                pb = new LightweightProcessBuilder(val);
                                pb.addPid(val);
                                processBuilders.add(pb);
                                idxPidMap.put(id.getAndIncrement(), val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;
                    case "process.admin.port":
                        MutableInt index = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            try {
                                int i = index.getAndIncrement();
                                LightweightProcessBuilder pb = processBuilders.get(i);
                                pb.addAdminPort(val);
                                adminPortMap.put(idxPidMap.get(i), Integer.valueOf(val));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;
                    case "process.data.port":
                        index = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            try {
                                LightweightProcessBuilder pb = processBuilders.get(index.getAndIncrement());
                                pb.addDataPort(val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;
                    case "process.host.name":
                        index = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            try {
                                int i = index.getAndIncrement();
                                LightweightProcessBuilder pb = processBuilders.get(i);
                                pb.addHostname(val);
                                localhostMap.put(idxPidMap.get(i), val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;
                    case "process.blocking":
                        index = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            try {
                                LightweightProcessBuilder pb = processBuilders.get(index.getAndIncrement());
                                pb.addBlockingParam(val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;
                    case "process.dependency":
                        index = new MutableInt(0);
                        Arrays.stream(properties.getProperty(prop).split(",")).forEach(val
                                -> {
                            try {
                                int idx = index.getAndIncrement();
                                Arrays.stream(val.split(":"))
                                        .forEach(s ->
                                        {
                                            dependencySet.computeIfAbsent(idx, integer -> Sets.newHashSet()).add(s);
                                            dependencySet.get(idx).add(s);
                                        });
                                LightweightProcess lightweightProcess = processBuilders.get(idx)
                                        .addDependentProcesses(dependencySet.get(idx))
                                        .build();
                                lightweightProcess
                                        .getDependentProcesses()
                                        .addAll(dependencySet.get(idx));
                                lwProcesses.add(lightweightProcess);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                        break;

                }

            });
            exec(lwProcesses);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void exec(Collection<LightweightProcess> colls) {
        colls.forEach( c -> {

            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path") + ":src/main/java";
            String className = c.getClass().getCanonicalName();

            List<String> command = Lists.newArrayList(javaBin, "-cp", classpath, className);
            logger.info("starting process now for "+c.pid());
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = null;
            try {
                process = processBuilder.inheritIO().start();
                processes.add(process);
                Thread.sleep(1000L);
                logger.info("Process started for {}", c.pid());
            } catch (Exception e) {
                e.printStackTrace();
            }



        });
    }

    public static int getAdminPort(String pid){
        return adminPortMap.get(pid);
    }

    public static String getHost(String pid){
        return localhostMap.get(pid);
    }
    public static void main(String[] args) {
        logger.info("Starting orchestrator ..");
        Orchestrator orchestrator = new Orchestrator();
        orchestrator.init();
        Scanner in = new Scanner(System.in);
        logger.info("******************************************************");
        logger.info("Enter X to exit :  ");
        logger.info("******************************************************");
        String input = in.nextLine();
        if(StringUtils.equalsIgnoreCase("X", input)){
            orchestrator.processes.forEach(Process::destroy);
        }

    }
}

