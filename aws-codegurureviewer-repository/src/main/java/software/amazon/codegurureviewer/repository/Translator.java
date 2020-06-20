package software.amazon.codegurureviewer.repository;

import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.CodeCommitRepository;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.Repository;
import software.amazon.awssdk.services.codegurureviewer.model.ThirdPartySourceRepository;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AssociateRepositoryRequest translateToAssociateRepositoryRequest(final ResourceModel model) {
        Repository repository = null;
        String providerType = model.getProviderType();
        if (providerType.equals(ProviderType.CODE_COMMIT)) {
            repository = Repository.builder().codeCommit(
                    CodeCommitRepository.builder()
                            .name(model.getName())
                            .build()
            ).build();
        } else if (providerType.equals(ProviderType.BITBUCKET)) {
            repository = Repository.builder().bitbucket(
                    ThirdPartySourceRepository.builder()
                            .name(model.getName())
                            .connectionArn(model.getConnectionArn())
                            .owner(model.getOwner())
                            .build()
            ).build();
        } else {
            throw new CfnInvalidRequestException(String.format("Unknown ProviderType of %s", model.getProviderType()));
        }

        return AssociateRepositoryRequest.builder().repository(repository).build();
    }
}