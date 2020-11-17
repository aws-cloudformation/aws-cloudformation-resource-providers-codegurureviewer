package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.cloudformation.LambdaWrapper;

public class CodeGuruReviewerClientBuilder {

    public static CodeGuruReviewerClient getClient() {
        return CodeGuruReviewerClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
