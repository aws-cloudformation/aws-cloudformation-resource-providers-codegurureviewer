package software.amazon.codegurureviewer.repositoryassociation;

import lombok.Setter;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.NotFoundException;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    // Allow current Lambda Handler runner to call stabilizeOnHandle this many times
    private final static int MAX_STABILIZE_ATTEMPTS = 5;
    // Wait time in between each stabilizeOnHandle call. Average time for repository association is around 25 seconds
    // and the Lambda handler timeout time is 1 min so ideally the MAX_STABILIZED_ATTEMPS * STABILIZE_SLEEP_TIME_MS
    // should be between 25,000 - 60,000 ms (25 - 60 seconds). In case the Lambda times out, stabilization will be
    // called again in the new Lambda call.
    private final static Duration STABILIZE_SLEEP_TIME_MS = Duration.ofMillis(7000);

    private final int maxStabilizeAttempts;
    private final Duration stabilizeSleepTimeMs;

    public BaseHandlerStd() {
        this.maxStabilizeAttempts = MAX_STABILIZE_ATTEMPTS;
        this.stabilizeSleepTimeMs = STABILIZE_SLEEP_TIME_MS;
    }

    public BaseHandlerStd(final int maxStabilizeAttempts, final Duration stabilizeSleepTimeMs) {
        this.maxStabilizeAttempts = maxStabilizeAttempts;
        this.stabilizeSleepTimeMs = stabilizeSleepTimeMs;
    }

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(CodeGuruReviewerClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final Logger logger);

    protected DescribeRepositoryAssociationResponse describeRepositoryAssociation(
            final DescribeRepositoryAssociationRequest describeRepositoryAssociationRequest,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model) {
        DescribeRepositoryAssociationResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(describeRepositoryAssociationRequest,
                    proxyClient.client()::describeRepositoryAssociation);
        } catch (final NotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName(), e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
        } catch (final ThrottlingException e) {
            throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
        }

        return awsResponse;
    }

    protected boolean stabilizeOnHandle(
            final AwsRequest awsRequest,
            final AwsResponse awsResponse,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        return true;
    };

    protected boolean stabilizeLoop(
            final AwsRequest awsRequest,
            final AwsResponse awsResponse,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext
    ) {
        boolean stabilized = false;
        int stabilizeAttempts = 0;

        while (!stabilized && stabilizeAttempts < maxStabilizeAttempts) {
            stabilized = stabilizeOnHandle(awsRequest, awsResponse, proxyClient, model, callbackContext);

            try {
                Thread.sleep(stabilizeSleepTimeMs.toMillis());
            } catch (InterruptedException e) {
                throw new CfnInternalFailureException(e);
            }

            stabilizeAttempts += 1;
        }

        return stabilized;
    }
}
