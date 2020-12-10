package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.CodeCommitRepository;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.Repository;
import software.amazon.awssdk.services.codegurureviewer.model.ThirdPartySourceRepository;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        String providerType = model.getType();
        if (providerType.equals(ProviderType.CODE_COMMIT.toString())) {
            repository = Repository.builder().codeCommit(
                    CodeCommitRepository.builder()
                            .name(model.getName())
                            .build()
            ).build();
        } else if (providerType.equals(ProviderType.BITBUCKET.toString())) {
            repository = Repository.builder().bitbucket(
                    ThirdPartySourceRepository.builder()
                            .name(model.getName())
                            .connectionArn(model.getConnectionArn())
                            .owner(model.getOwner())
                            .build()
            ).build();
        } else if (providerType.equals(ProviderType.GIT_HUB_ENTERPRISE_SERVER.toString())) {
            repository = Repository.builder().gitHubEnterpriseServer(
                    ThirdPartySourceRepository.builder()
                            .name(model.getName())
                            .connectionArn(model.getConnectionArn())
                            .owner(model.getOwner())
                            .build()
            ).build();
        } else {
            throw new CfnInvalidRequestException(String.format("Unknown Type of %s", model.getType()));
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

    static DescribeRepositoryAssociationRequest translateToDescribeRepositoryAssociationRequest(final AssociateRepositoryResponse associateRepositoryResponse) {
        return DescribeRepositoryAssociationRequest.builder()
                .associationArn(associateRepositoryResponse.repositoryAssociation().associationArn()).build();
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
        if(!providerType.equals(ProviderType.CODE_COMMIT)) {
            resourceModelBuilder.owner(awsResponse.repositoryAssociation().owner());
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

    private static Optional<Map<String, String>> getTagsFromModel(final ResourceModel model) {
        List<Tag> tags = model.getTags();
        if (tags == null || tags.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tags.stream().collect(Collectors.toMap(Tag :: getKey, Tag :: getValue)));
    }
}
