package com.dazednconfused.catalauncher.helper.sysinfo;

/**
 * Utility class comprising this {@link System}'s information.
 * */
public class SystemInformation {

    private String javaVersion;
    public String javaVendor;
    public String javaVendorUrl;
    public String javaHome;
    public String javaVmSpecificationVersion;
    public String javaVmSpecificationVendor;
    public String javaVmSpecificationName;
    public String javaVmVersion;
    public String javaVmVendor;
    public String javaVmName;
    public String javaSpecificationVersion;
    public String javaSpecificationVendor;
    public String javaSpecificationName;
    public String javaClassVersion;
    public String javaClasspath;
    public String javaLibraryPath;
    public String javaIoTmpDir;
    public String javaCompiler;
    public String javaExtDirs;
    public String osName;
    public String osArch;
    public String osVersion;
    public String fileSeparator;
    public String pathSeparator;
    public String lineSeparator;
    public String userName;
    public String userHome;
    public String userDir;

    /**
     * Private constructor.
     * */
    private SystemInformation() {
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public String getJavaVendorUrl() {
        return javaVendorUrl;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJavaVmSpecificationVersion() {
        return javaVmSpecificationVersion;
    }

    public String getJavaVmSpecificationVendor() {
        return javaVmSpecificationVendor;
    }

    public String getJavaVmSpecificationName() {
        return javaVmSpecificationName;
    }

    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    public String getJavaVmVendor() {
        return javaVmVendor;
    }

    public String getJavaVmName() {
        return javaVmName;
    }

    public String getJavaSpecificationVersion() {
        return javaSpecificationVersion;
    }

    public String getJavaSpecificationVendor() {
        return javaSpecificationVendor;
    }

    public String getJavaSpecificationName() {
        return javaSpecificationName;
    }

    public String getJavaClassVersion() {
        return javaClassVersion;
    }

    public String getJavaClasspath() {
        return javaClasspath;
    }

    public String getJavaLibraryPath() {
        return javaLibraryPath;
    }

    public String getJavaIoTmpDir() {
        return javaIoTmpDir;
    }

    public String getJavaCompiler() {
        return javaCompiler;
    }

    public String getJavaExtDirs() {
        return javaExtDirs;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getFileSeparator() {
        return fileSeparator;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserHome() {
        return userHome;
    }

    public String getUserDir() {
        return userDir;
    }

    public static final class SystemInformationBuilder {

        private String javaVersion;
        private String javaVendor;
        private String javaVendorUrl;
        private String javaHome;
        private String javaVmSpecificationVersion;
        private String javaVmSpecificationVendor;
        private String javaVmSpecificationName;
        private String javaVmVersion;
        private String javaVmVendor;
        private String javaVmName;
        private String javaSpecificationVersion;
        private String javaSpecificationVendor;
        private String javaSpecificationName;
        private String javaClassVersion;
        private String javaClasspath;
        private String javaLibraryPath;
        private String javaIoTmpDir;
        private String javaCompiler;
        private String javaExtDirs;
        private String osName;
        private String osArch;
        private String osVersion;
        private String fileSeparator;
        private String pathSeparator;
        private String lineSeparator;
        private String userName;
        private String userHome;
        private String userDir;

        private SystemInformationBuilder() {
        }

        public static SystemInformationBuilder newSystemInformation() {
            return new SystemInformationBuilder();
        }

        public SystemInformationBuilder javaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
            return this;
        }

        public SystemInformationBuilder javaVendor(String javaVendor) {
            this.javaVendor = javaVendor;
            return this;
        }

        public SystemInformationBuilder javaVendorUrl(String javaVendorUrl) {
            this.javaVendorUrl = javaVendorUrl;
            return this;
        }

        public SystemInformationBuilder javaHome(String javaHome) {
            this.javaHome = javaHome;
            return this;
        }

        public SystemInformationBuilder javaVmSpecificationVersion(String javaVmSpecificationVersion) {
            this.javaVmSpecificationVersion = javaVmSpecificationVersion;
            return this;
        }

        public SystemInformationBuilder javaVmSpecificationVendor(String javaVmSpecificationVendor) {
            this.javaVmSpecificationVendor = javaVmSpecificationVendor;
            return this;
        }

        public SystemInformationBuilder javaVmSpecificationName(String javaVmSpecificationName) {
            this.javaVmSpecificationName = javaVmSpecificationName;
            return this;
        }

        public SystemInformationBuilder javaVmVersion(String javaVmVersion) {
            this.javaVmVersion = javaVmVersion;
            return this;
        }

        public SystemInformationBuilder javaVmVendor(String javaVmVendor) {
            this.javaVmVendor = javaVmVendor;
            return this;
        }

        public SystemInformationBuilder javaVmName(String javaVmName) {
            this.javaVmName = javaVmName;
            return this;
        }

        public SystemInformationBuilder javaSpecificationVersion(String javaSpecificationVersion) {
            this.javaSpecificationVersion = javaSpecificationVersion;
            return this;
        }

        public SystemInformationBuilder javaSpecificationVendor(String javaSpecificationVendor) {
            this.javaSpecificationVendor = javaSpecificationVendor;
            return this;
        }

        public SystemInformationBuilder javaSpecificationName(String javaSpecificationName) {
            this.javaSpecificationName = javaSpecificationName;
            return this;
        }

        public SystemInformationBuilder javaClassVersion(String javaClassVersion) {
            this.javaClassVersion = javaClassVersion;
            return this;
        }

        public SystemInformationBuilder javaClasspath(String javaClasspath) {
            this.javaClasspath = javaClasspath;
            return this;
        }

        public SystemInformationBuilder javaLibraryPath(String javaLibraryPath) {
            this.javaLibraryPath = javaLibraryPath;
            return this;
        }

        public SystemInformationBuilder javaIoTmpDir(String javaIoTmpDir) {
            this.javaIoTmpDir = javaIoTmpDir;
            return this;
        }

        public SystemInformationBuilder javaCompiler(String javaCompiler) {
            this.javaCompiler = javaCompiler;
            return this;
        }

        public SystemInformationBuilder javaExtDirs(String javaExtDirs) {
            this.javaExtDirs = javaExtDirs;
            return this;
        }

        public SystemInformationBuilder osName(String osName) {
            this.osName = osName;
            return this;
        }

        public SystemInformationBuilder osArch(String osArch) {
            this.osArch = osArch;
            return this;
        }

        public SystemInformationBuilder osVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public SystemInformationBuilder fileSeparator(String fileSeparator) {
            this.fileSeparator = fileSeparator;
            return this;
        }

        public SystemInformationBuilder pathSeparator(String pathSeparator) {
            this.pathSeparator = pathSeparator;
            return this;
        }

        public SystemInformationBuilder lineSeparator(String lineSeparator) {
            this.lineSeparator = lineSeparator;
            return this;
        }

        public SystemInformationBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public SystemInformationBuilder userHome(String userHome) {
            this.userHome = userHome;
            return this;
        }

        public SystemInformationBuilder userDir(String userDir) {
            this.userDir = userDir;
            return this;
        }

        /**
         * Builds a new {@link SystemInformation} instance.
         * */
        public SystemInformation build() {

            SystemInformation systemInformation = new SystemInformation();

            systemInformation.javaVersion = this.javaVersion;
            systemInformation.javaVmVersion = this.javaVmVersion;
            systemInformation.javaHome = this.javaHome;
            systemInformation.javaIoTmpDir = this.javaIoTmpDir;
            systemInformation.javaSpecificationVersion = this.javaSpecificationVersion;
            systemInformation.osVersion = this.osVersion;
            systemInformation.javaLibraryPath = this.javaLibraryPath;
            systemInformation.javaVendor = this.javaVendor;
            systemInformation.fileSeparator = this.fileSeparator;
            systemInformation.javaVmSpecificationName = this.javaVmSpecificationName;
            systemInformation.javaExtDirs = this.javaExtDirs;
            systemInformation.userHome = this.userHome;
            systemInformation.javaClassVersion = this.javaClassVersion;
            systemInformation.javaClasspath = this.javaClasspath;
            systemInformation.javaVmSpecificationVendor = this.javaVmSpecificationVendor;
            systemInformation.javaSpecificationName = this.javaSpecificationName;
            systemInformation.javaVmName = this.javaVmName;
            systemInformation.userName = this.userName;
            systemInformation.osName = this.osName;
            systemInformation.osArch = this.osArch;
            systemInformation.lineSeparator = this.lineSeparator;
            systemInformation.javaVmVendor = this.javaVmVendor;
            systemInformation.javaVmSpecificationVersion = this.javaVmSpecificationVersion;
            systemInformation.javaSpecificationVendor = this.javaSpecificationVendor;
            systemInformation.userDir = this.userDir;
            systemInformation.pathSeparator = this.pathSeparator;
            systemInformation.javaVendorUrl = this.javaVendorUrl;
            systemInformation.javaCompiler = this.javaCompiler;

            return systemInformation;
        }
    }
}