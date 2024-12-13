package edu.stanford.protege.versioning.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.versioning.entity.*;
import edu.stanford.protege.versioning.owl.commands.*;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.IriDeserializer;
import edu.stanford.protege.webprotege.jackson.*;
import edu.stanford.protege.webprotege.project.*;
import io.minio.MinioClient;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.util.UrlPathHelper;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@Configuration
public class ApplicationBeans implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    @Bean
    CommandExecutor<CreateNewProjectAction, CreateNewProjectResult> createNewProjectExecutor() {
        return new CommandExecutorImpl<>(CreateNewProjectResult.class);
    }

    @Bean
    MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .endpoint(properties.getEndPoint())
                .build();
    }

    @Bean
    CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> executorForPostCoordination() {
        return new CommandExecutorImpl<>(GetAllOwlClassesResponse.class);
    }


    @Bean
    CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityRequest() {
        return new CommandExecutorImpl<>(GetProjectEntityInfoResponse.class);
    }
}
