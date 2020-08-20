package software.amazon.codegurureviewer.repositoryassociation;

import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.OperationStatus;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    private static String ASSOCIATION_ARN = "associationArn";
    private static String REPO_NAME = "repoName";
    private static String OWNER = "owner";


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
    public void translateFromReadResponse_CodeCommit() {
        RepositoryAssociation repositoryAssociation = RepositoryAssociation.builder()
                                                                           .associationArn(ASSOCIATION_ARN)
                                                                           .name(REPO_NAME)
                                                                           .owner(OWNER)
                                                                           .providerType(ProviderType.CODE_COMMIT)
                                                                           .build();
        DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse = DescribeRepositoryAssociationResponse.builder()
                                                                                                                           .repositoryAssociation(repositoryAssociation)
                                                                                                                           .build();

        ResourceModel resourceModel = Translator.translateFromReadResponse(describeRepositoryAssociationResponse);

        assertThat(resourceModel.getAssociationArn()).isEqualTo(ASSOCIATION_ARN);
        assertThat(resourceModel.getName()).isEqualTo(REPO_NAME);
        assertThat(resourceModel.getOwner()).isNull();
        assertThat(resourceModel.getType()).isEqualTo(ProviderType.CODE_COMMIT.toString());
    }

    @Test
    public void translateFromReadResponse_Bitbucket() {
        RepositoryAssociation repositoryAssociation = RepositoryAssociation.builder()
                                                                           .associationArn(ASSOCIATION_ARN)
                                                                           .name(REPO_NAME)
                                                                           .owner(OWNER)
                                                                           .providerType(ProviderType.BITBUCKET)
                                                                           .build();
        DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse = DescribeRepositoryAssociationResponse.builder()
                                                                                                                           .repositoryAssociation(repositoryAssociation)
                                                                                                                           .build();

        ResourceModel resourceModel = Translator.translateFromReadResponse(describeRepositoryAssociationResponse);

        assertThat(resourceModel.getAssociationArn()).isEqualTo(ASSOCIATION_ARN);
        assertThat(resourceModel.getName()).isEqualTo(REPO_NAME);
        assertThat(resourceModel.getOwner()).isEqualTo(OWNER);
        assertThat(resourceModel.getType()).isEqualTo(ProviderType.BITBUCKET.toString());
    }

    @Test
    public void constructor() {
        new Translator();
    }

}
