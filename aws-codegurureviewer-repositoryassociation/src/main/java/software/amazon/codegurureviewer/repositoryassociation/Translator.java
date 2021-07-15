package software.amazon.codegurureviewer.repositoryassociation;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.Repository;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationSummary;
import software.amazon.awssdk.services.codegurureviewer.model.ThirdPartySourceRepository;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public final class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AssociateRepositoryRequest translateToAssociateRepositoryRequest(final ResourceModel model) {
        Repository repository = null;
        ProviderType providerType = ProviderType.fromValue(model.getType());
        switch(providerType) {
            case CODE_COMMIT:
                repository = getCodeCommitRepository(model);
                break;
            case BITBUCKET:
                repository = Repository.builder().bitbucket(getThirdPartyRepository(model)).build();
                break;
            case GIT_HUB_ENTERPRISE_SERVER:
                repository = Repository.builder().gitHubEnterpriseServer(getThirdPartyRepository(model)).build();
                break;
            case S3_BUCKET:
                repository = getS3BucketRepository(model);
                break;
            default:
                throw new CfnInvalidRequestException(String.format("Unknown Type of %s", providerType));
        }

        final Optional<Map<String, String>> tags = getTagsFromModel(model);
        if(tags.isPresent()) {
            return AssociateRepositoryRequest.builder()
                    .repository(repository)
                    .tags(tags.get())
                    .build();
        }

        return AssociateRepositoryRequest.builder().repository(repository).build();
    }

    static Repository getS3BucketRepository(final ResourceModel model) {
        if (StringUtils.isNullOrEmpty(model.getBucketName())) {
            throw new CfnInvalidRequestException("BucketName is required for S3Bucket repository.");
        }
        return Repository.builder()
                .s3Bucket(repository -> repository.name(model.getName()).bucketName(model.getBucketName()))
                .build();
    }

    static Repository getCodeCommitRepository(ResourceModel model) {
        return Repository.builder().codeCommit(repository -> repository.name(model.getName())).build();
    }

    static ThirdPartySourceRepository getThirdPartyRepository(ResourceModel model) {
        return ThirdPartySourceRepository.builder()
                .name(model.getName())
                .connectionArn(model.getConnectionArn())
                .owner(model.getOwner())
                .build();
    }

    static DescribeRepositoryAssociationRequest translateToDescribeRepositoryAssociationRequest(final AssociateRepositoryResponse associateRepositoryResponse) {
        return DescribeRepositoryAssociationRequest.builder()
                .associationArn(associateRepositoryResponse.repositoryAssociation().associationArn())
                .build();
    }

    static DescribeRepositoryAssociationRequest translateToDescribeRepositoryAssociationRequest(final ResourceModel model) {
        return DescribeRepositoryAssociationRequest.builder()
                .associationArn(model.getAssociationArn()).build();
    }

    static ResourceModel translateFromReadResponse(final DescribeRepositoryAssociationResponse awsResponse) {
        ResourceModel.ResourceModelBuilder resourceModelBuilder =  ResourceModel.builder()
                                                                                .associationArn(awsResponse.repositoryAssociation().associationArn())
                                                                                .name(awsResponse.repositoryAssociation().name())
                                                                                .type(awsResponse.repositoryAssociation().providerType().toString())
                                                                                .connectionArn(awsResponse.repositoryAssociation().connectionArn());

        ProviderType providerType = awsResponse.repositoryAssociation().providerType();
        if(!(providerType.equals(ProviderType.CODE_COMMIT) || providerType.equals(ProviderType.S3_BUCKET))) {
            resourceModelBuilder.owner(awsResponse.repositoryAssociation().owner());
        }
        if (ProviderType.S3_BUCKET.equals(providerType)
                && awsResponse.repositoryAssociation() != null
                && awsResponse.repositoryAssociation().s3RepositoryDetails() != null) {
            resourceModelBuilder.bucketName(awsResponse.repositoryAssociation().s3RepositoryDetails().bucketName());
        }
        Map<String, String> tags = awsResponse.tags();
        if(!tags.isEmpty()) {
            resourceModelBuilder.tags(tags.entrySet()
                    .stream()
                    .map(tag -> new Tag(tag.getKey(), tag.getValue()))
                    .collect(Collectors.toList()));
        }
        return resourceModelBuilder.build();
    }

    static DisassociateRepositoryRequest translateToDisassociateRepositoryRequest(final ResourceModel model) {
        return DisassociateRepositoryRequest.builder()
                .associationArn(model.getAssociationArn())
                .build();
    }

    static ListRepositoryAssociationsRequest translateToLisRepositoryAssocationResquest(final String nextToken) {
        return ListRepositoryAssociationsRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    static List<ResourceModel> translateFromListRepositoryAssocationResponse(final List<RepositoryAssociationSummary> summaries) {
        return streamOfOrEmpty(summaries)
                .map(repositoryAssociationSummary -> ResourceModel.builder()
                        .associationArn(repositoryAssociationSummary.associationArn())
                        .name(repositoryAssociationSummary.name())
                        .owner(repositoryAssociationSummary.owner())
                        .type(repositoryAssociationSummary.providerType().toString())
                        .connectionArn(repositoryAssociationSummary.connectionArn())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private static Optional<Map<String, String>> getTagsFromModel(final ResourceModel model) {
        List<Tag> tags = model.getTags();
        if (tags == null || tags.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tags.stream().collect(Collectors.toMap(Tag :: getKey, Tag :: getValue)));
    }
}
