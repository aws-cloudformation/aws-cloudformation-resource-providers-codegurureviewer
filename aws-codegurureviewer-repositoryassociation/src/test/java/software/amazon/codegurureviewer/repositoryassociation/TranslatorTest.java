package software.amazon.codegurureviewer.repositoryassociation;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.OperationStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    private static String ASSOCIATION_ARN = "associationArn";
    private static String REPO_NAME = "repoName";
    private static String OWNER = "owner";
    private static Map<String, String> TAGS_MAP = ImmutableMap.of("key1", "value1", "key2", "value2");
    private static List<Tag> TAGS = new ArrayList<>(Arrays.asList(new Tag("key1", "value1"), new Tag("key2", "value2")));

    @Test
    public void translateFromReadResponse_WithTags() {
        RepositoryAssociation repositoryAssociation = RepositoryAssociation.builder()
                .associationArn(ASSOCIATION_ARN)
                .name(REPO_NAME)
                .providerType(ProviderType.CODE_COMMIT)
                .build();
        DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse = DescribeRepositoryAssociationResponse.builder()
                .repositoryAssociation(repositoryAssociation)
                .tags(TAGS_MAP)
                .build();

        ResourceModel resourceModel = Translator.translateFromReadResponse(describeRepositoryAssociationResponse);

        assertThat(resourceModel.getAssociationArn()).isEqualTo(ASSOCIATION_ARN);
        assertThat(resourceModel.getName()).isEqualTo(REPO_NAME);
        assertThat(resourceModel.getType()).isEqualTo(ProviderType.CODE_COMMIT.toString());
        assertThat(resourceModel.getOwner()).isNull();
        assertThat(resourceModel.getTags()).isEqualTo(TAGS);
    }

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

    @ParameterizedTest
    @ValueSource(strings = {"CodeCommit", "Bitbucket"})
    public void translateToAssociateRepositoryRequest_WithTag(String providerType) {
        AssociateRepositoryRequest associateRepositoryRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(providerType).tags(TAGS).build());
        assertNotNull(associateRepositoryRequest.tags());
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
