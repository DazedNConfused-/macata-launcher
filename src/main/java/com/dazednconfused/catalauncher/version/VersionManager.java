package com.dazednconfused.catalauncher.version;

import com.dazednconfused.catalauncher.helper.GitInfoManager;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.io.File;
import java.io.FileReader;
import java.util.Optional;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionManager.class);

    private static final String META_INF_PATH = "/META-INF/maven/com.dazednconfused/macatalauncher/pom.properties";

    private static GitInfoManager instance;
    private static final Object lock = new Object(); //thread-safety singleton lock

    public static Result<Throwable, String> getCurrentVersionFromPomInformation() {
        Result<Throwable, Model> modelFromPomResult = Try.of(() -> new File("pom.xml"))
            .filter(File::exists)
            .mapTry(file -> new MavenXpp3Reader().read(new FileReader(file)))
            .map(Result::success).recover(Result::failure)
            .get();

        if (modelFromPomResult.toEither().isRight()) {
            return modelResultMapper(modelFromPomResult);
        }

        LOGGER.debug("Could not read current version from pom.xml. Will try to read it from META-INF properties...", modelFromPomResult.toEither().getLeft().getError());

        Result<Throwable, Model> modelFromMetaInfResult = Try.of(() ->  new File(META_INF_PATH))
            .filter(File::exists)
            .mapTry(file -> new MavenXpp3Reader().read(new FileReader(file)))
            .map(Result::success).recover(Result::failure)
            .get();

        if (modelFromPomResult.toEither().isRight()) {
            return modelResultMapper(modelFromMetaInfResult);
        }

        return Result.failure(new Throwable(String.format("Could not retrieve version number from neither the pom.xml nor the META-INF path [%s]", META_INF_PATH)));
    }

    private static Result<Throwable, String> modelResultMapper(Result<Throwable, Model> r) {
        if (r.toEither().isRight()) {
            Optional<Model> result = r.toEither().get().getResult();
            if (result.isPresent()) {
                return Result.success(result.map(Model::getVersion).orElseThrow());
            } else {
                return Result.failure(new Throwable("Could not retrieve version because underlying Model was empty! This is an odd state."));
            }
        }

        return Result.failure(r.toEither().getLeft().getError());
    }

}
