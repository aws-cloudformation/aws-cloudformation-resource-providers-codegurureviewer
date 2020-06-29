package software.amazon.codegurureviewer.repositoryassociation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Test
    public void translateToAssociateRepositoryRequest_ValidRepositories() {

        AssociateRepositoryRequest bitBucketRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.BITBUCKET.toString()).build());
        assertNotNull(bitBucketRequest.repository().bitbucket());
        assertNull(bitBucketRequest.repository().codeCommit());

        AssociateRepositoryRequest codeCommitRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.CODE_COMMIT.toString()).build());
        assertNotNull(codeCommitRequest.repository().codeCommit());
        assertNull(codeCommitRequest.repository().bitbucket());
    }

    @Test
    public void translateToAssociateRepositoryRequest_InValidRepositories() {
        assertThatExceptionOfType(CfnInvalidRequestException.class)
                .isThrownBy(() -> Translator.translateToAssociateRepositoryRequest(ResourceModel.builder()
                        .type("Invalid Type")
                        .build()));

    }

    @Test
    public void constructor() {
        new Translator();
    }

}
