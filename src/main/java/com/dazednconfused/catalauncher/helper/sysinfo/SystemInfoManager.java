package com.dazednconfused.catalauncher.helper.sysinfo;

import static com.dazednconfused.catalauncher.helper.sysinfo.SystemInformation.SystemInformationBuilder.newSystemInformation;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vavr.control.Try;

import org.apache.log4j.Level;
import org.slf4j.Logger;

public class SystemInfoManager {

    /**
     * The version of Java Runtime Environment.
     */
    private static final String JAVA_VERSION = "java.version";

    /**
     * The name of Java Runtime Environment vendor.
     * */
    private static final String JAVA_VENDOR = "java.vendor";

    /**
     * The URL of Java vendor.
     * */
    private static final String JAVA_VENDOR_URL = "java.vendor.url";

    /**
     * The directory of Java installation.
     * */
    private static final String JAVA_HOME = "java.home";

    /**
     * The specification version of Java Virtual Machine.
     * */
    private static final String JAVA_VM_SPECIFICATION_VERSION = "java.vm.specification.version";

    /**
     * The name of specification vendor of Java Virtual Machine.
     * */
    private static final String JAVA_VM_SPECIFICATION_VENDOR = "java.vm.specification.vendor";

    /**
     * Java Virtual Machine specification name.
     * */
    private static final String JAVA_VM_SPECIFICATION_NAME = "java.vm.specification.name";

    /**
     * JVM implementation version.
     * */
    private static final String JAVA_VM_VERSION = "java.vm.version";

    /**
     * JVM implementation vendor.
     * */
    private static final String JAVA_VM_VENDOR = "java.vm.vendor";

    /**
     * JVM  implementation name.
     * */
    private static final String JAVA_VM_NAME = "java.vm.name";

    /**
     * The name of specification version Java Runtime Environment.
     * */
    private static final String JAVA_SPECIFICATION_VERSION = "java.specification.version";

    /**
     * JRE specification vendor.
     * */
    private static final String JAVA_SPECIFICATION_VENDOR = "java.specification.vendor";

    /**
     * JRE specification name.
     * */
    private static final String JAVA_SPECIFICATION_NAME = "java.specification.name";

    /**
     * Java class format version number.
     * */
    private static final String JAVA_CLASS_VERSION = "java.class.version";

    /**
     * Path of java class.
     * */
    private static final String JAVA_CLASS_PATH = "java.class.path";

    /**
     * List of paths to search when loading libraries.
     * */
    private static final String JAVA_LIBRARY_PATH = "java.library.path";

    /**
     * The path of temp file.
     * */
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    /**
     * The Name of JIT compiler to use.
     * */
    private static final String JAVA_COMPILER = "java.compiler";

    /**
     * The path of extension directory or directories.
     * */
    private static final String JAVA_EXT_DIRS = "java.ext.dirs";

    /**
     * The name of OS name.
     * */
    private static final String OS_NAME = "os.name";

    /**
     * The OS architecture.
     * */
    private static final String OS_ARCH = "os.arch";

    /**
     * The version of OS.
     * */
    private static final String OS_VERSION = "os.version";

    /**
     * The File separator.
     * */
    private static final String FILE_SEPARATOR = "file.separator";

    /**
     * The path separator.
     * */
    private static final String PATH_SEPARATOR = "path.separator";

    /**
     * The line separator.
     * */
    private static final String LINE_SEPARATOR = "line.separator";

    /**
     * The name of account name user.
     * */
    private static final String USER_NAME = "user.name";

    /**
     * The home directory of user.
     * */
    private static final String USER_HOME = "user.home";

    /**
     * The current working directory of the user.
     * */
    private static final String USER_DIR = "user.dir";

    /**
     * Logs current {@link System} information using the provided {@link org.apache.log4j.Logger}. Defaults to {@link Level#DEBUG}
     * if no valid {@code logLevel} is provided.
     * */
    public static void logSystemInformation(Logger logger, Level logLevel) {
        Try.of(() -> new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getSystemInformation()))
            .andThen(sysInfo -> {
                if (Level.TRACE == logLevel) {
                    logger.trace(sysInfo);
                } else if (Level.INFO == logLevel) {
                    logger.info(sysInfo);
                } else {
                    logger.debug(sysInfo);
                }
            })
            .onFailure(throwable -> logger.error("There was an error during the serialization of this system's information", throwable));
    }

    /**
     * Returns the current {@link System}'s information for debugging purposes.
     * */
    public static SystemInformation getSystemInformation() {
        return newSystemInformation()
            .javaVersion(System.getProperty(JAVA_VERSION))
            .javaVendor(System.getProperty(JAVA_VENDOR))
            .javaVendorUrl(System.getProperty(JAVA_VENDOR_URL))
            .javaHome(System.getProperty(JAVA_HOME))
            .javaVmSpecificationVersion(System.getProperty(JAVA_VM_SPECIFICATION_VERSION))
            .javaVmSpecificationVendor(System.getProperty(JAVA_VM_SPECIFICATION_VENDOR))
            .javaSpecificationName(System.getProperty(JAVA_VM_SPECIFICATION_NAME))
            .javaVmVersion(System.getProperty(JAVA_VM_VERSION))
            .javaVmVendor(System.getProperty(JAVA_VM_VENDOR))
            .javaVmName(System.getProperty(JAVA_VM_NAME))
            .javaSpecificationVersion(System.getProperty(JAVA_SPECIFICATION_VERSION))
            .javaSpecificationVendor(System.getProperty(JAVA_SPECIFICATION_VENDOR))
            .javaVmSpecificationName(System.getProperty(JAVA_SPECIFICATION_NAME))
            .javaClassVersion(System.getProperty(JAVA_CLASS_VERSION))
            .javaClasspath(System.getProperty(JAVA_CLASS_PATH))
            .javaLibraryPath(System.getProperty(JAVA_LIBRARY_PATH))
            .javaIoTmpDir(System.getProperty(JAVA_IO_TMPDIR))
            .javaCompiler(System.getProperty(JAVA_COMPILER))
            .javaExtDirs(System.getProperty(JAVA_EXT_DIRS))
            .osName(System.getProperty(OS_NAME))
            .osArch(System.getProperty(OS_ARCH))
            .osVersion(System.getProperty(OS_VERSION))
            .fileSeparator(System.getProperty(FILE_SEPARATOR))
            .pathSeparator(System.getProperty(PATH_SEPARATOR))
            .lineSeparator(System.getProperty(LINE_SEPARATOR))
            .userName(System.getProperty(USER_NAME))
            .userHome(System.getProperty(USER_HOME))
            .userDir(System.getProperty(USER_DIR))
            .build();
    }
}
