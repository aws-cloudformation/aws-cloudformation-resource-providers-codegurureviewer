package software.amazon.codegurureviewer.repositoryassociation;


import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationSummary;
import software.amazon.awssdk.services.codegurureviewer.model.S3RepositoryDetails;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    private static final String ASSOCIATION_ARN = "associationArn";
    private static final String REPO_NAME = "repoName";
    private static final String OWNER = "owner";
    private static final String BUCKET_NAME = "bucketName";
    private static final Map<String, String> TAGS_MAP = ImmutableMap.of("key1", "value1", "key2", "value2");
    private static final List<Tag> TAGS = new ArrayList<>(Arrays.asList(new Tag("key1", "value1"), new Tag("key2", "value2")));

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
        assertNull(bitBucketRequest.repository().s3Bucket());

        AssociateRepositoryRequest codeCommitRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.CODE_COMMIT.toString()).build());
        assertNull(codeCommitRequest.repository().bitbucket());
        assertNotNull(codeCommitRequest.repository().codeCommit());
        assertNull(bitBucketRequest.repository().gitHubEnterpriseServer());
        assertNull(bitBucketRequest.repository().s3Bucket());

        AssociateRepositoryRequest gheRequest =
                Translator.translateToAssociateRepositoryRequest(ResourceModel.builder().type(ProviderType.GIT_HUB_ENTERPRISE_SERVER.toString()).build());
        assertNull(gheRequest.repository().bitbucket());
        assertNull(gheRequest.repository().codeCommit());
        assertNotNull(gheRequest.repository().gitHubEnterpriseServer());
        assertNull(gheRequest.repository().s3Bucket());

        AssociateRepositoryRequest s3Request = Translator.translateToAssociateRepositoryRequest(
                ResourceModel.builder().type(ProviderType.S3_BUCKET.toString()).bucketName(BUCKET_NAME).name(REPO_NAME).build());
        assertNull(s3Request.repository().bitbucket());
        assertNull(s3Request.repository().codeCommit());
        assertNull(s3Request.repository().gitHubEnterpriseServer());
        assertNotNull(s3Request.repository().s3Bucket());
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
    public void translateToAssociateRepositoryRequest_s3Repository() {
        AssociateRepositoryRequest s3RequestWithBucketName = Translator.translateToAssociateRepositoryRequest(
                ResourceModel.builder()
                        .type(ProviderType.S3_BUCKET.toString())
                        .bucketName(BUCKET_NAME)
                        .name(REPO_NAME)
                        .build());
        assertNull(s3RequestWithBucketName.repository().bitbucket());
        assertNull(s3RequestWithBucketName.repository().codeCommit());
        assertNull(s3RequestWithBucketName.repository().gitHubEnterpriseServer());
        assertNotNull(s3RequestWithBucketName.repository().s3Bucket());
        assertEquals(BUCKET_NAME, s3RequestWithBucketName.repository().s3Bucket().bucketName());
        assertEquals(REPO_NAME, s3RequestWithBucketName.repository().s3Bucket().name());
    }

    @Test
    public void translateToAssociateRepositoryRequest_s3Repository_missingBucketName() {

        CfnInvalidRequestException exception = assertThrows(CfnInvalidRequestException.class,
                () -> Translator.translateToAssociateRepositoryRequest(
                        ResourceModel.builder().type(ProviderType.S3_BUCKET.toString()).name(REPO_NAME).build()));
        assertEquals("Invalid request provided: BucketName is required for S3Bucket repository.", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = ProviderType.class)
    public void translateFromReadResponse(ProviderType providerType) {
        RepositoryAssociation repositoryAssociation = RepositoryAssociation.builder()
                .associationArn(ASSOCIATION_ARN)
                .name(REPO_NAME)
                .owner(OWNER)
                .providerType(providerType)
                .s3RepositoryDetails(S3RepositoryDetails.builder().bucketName(BUCKET_NAME).build())
                .build();

        DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse =
                DescribeRepositoryAssociationResponse.builder()
                        .repositoryAssociation(repositoryAssociation)
                        .build();

        ResourceModel resourceModel = Translator.translateFromReadResponse(describeRepositoryAssociationResponse);

        assertThat(resourceModel.getAssociationArn()).isEqualTo(ASSOCIATION_ARN);
        assertThat(resourceModel.getName()).isEqualTo(REPO_NAME);
        assertThat(resourceModel.getType()).isEqualTo(providerType.toString());
        if (ProviderType.CODE_COMMIT == providerType || ProviderType.S3_BUCKET == providerType) {
            assertThat(resourceModel.getOwner()).isNull();
        } else {
            assertThat(resourceModel.getOwner()).isEqualTo(OWNER);
        }
        if (ProviderType.S3_BUCKET.equals(providerType)) {
            assertEquals(BUCKET_NAME, resourceModel.getBucketName());
        } else {
            assertNull(resourceModel.getBucketName());
        }
    }

    @Test
    public void constructor() {
        new Translator();
    }

    @Test
    public void translateToLisRepositoryAssocationResquestTest() {
        final ListRepositoryAssociationsRequest request = ListRepositoryAssociationsRequest.builder()
                .nextToken("nextToken")
                .build();

        assertThat(Translator.translateToLisRepositoryAssocationResquest("nextToken"))
                .isEqualToComparingFieldByField(request);
    }

    @Test
    public void translateFromListRepositoryAssocationResponseTest() {
        final RepositoryAssociationSummary summary1 = RepositoryAssociationSummary.builder()
                .associationArn(ASSOCIATION_ARN)
                .providerType(ProviderType.CODE_COMMIT.toString())
                .name(REPO_NAME)
                .owner(OWNER)
                .build();
        final RepositoryAssociationSummary summary2 = RepositoryAssociationSummary.builder()
                .associationArn(ASSOCIATION_ARN + "1")
                .providerType(ProviderType.GIT_HUB.toString())
                .name(REPO_NAME)
                .owner(OWNER)
                .build();

        final ResourceModel model1 = ResourceModel.builder()
                .associationArn(ASSOCIATION_ARN)
                .type(ProviderType.CODE_COMMIT.toString())
                .name(REPO_NAME)
                .owner(OWNER)
                .build();
        final ResourceModel model2 = ResourceModel.builder()
                .associationArn(ASSOCIATION_ARN + "1")
                .type(ProviderType.GIT_HUB.toString())
                .name(REPO_NAME)
                .owner(OWNER)
                .build();

        List<ResourceModel> result =
                Translator.translateFromListRepositoryAssocationResponse(Arrays.asList(summary1, summary2));

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsAll(Arrays.asList(model1, model2));
    }

    @Test
    public void translateFromListRepositoryAssocationResponseTest_EmptySummaries() {
        final List<RepositoryAssociationSummary> summaries = Arrays.asList();

        List<ResourceModel> result = Translator.translateFromListRepositoryAssocationResponse(summaries);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

}
