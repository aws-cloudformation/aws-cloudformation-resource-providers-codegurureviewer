package software.amazon.codegurureviewer.repositoryassociation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

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
        assertNull(bitBucketRequest.repository().gitHubEnterpriseServer());

        AssociateRepositoryRequest codeCommitRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.CODE_COMMIT.toString()).build());
        assertNull(codeCommitRequest.repository().bitbucket());
        assertNotNull(codeCommitRequest.repository().codeCommit());
        assertNull(bitBucketRequest.repository().gitHubEnterpriseServer());

        AssociateRepositoryRequest gitHubEnterprise =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.GIT_HUB_ENTERPRISE_SERVER.toString()).build());
        assertNull(gitHubEnterprise.repository().bitbucket());
        assertNull(gitHubEnterprise.repository().codeCommit());
        assertNotNull(gitHubEnterprise.repository().gitHubEnterpriseServer());
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
