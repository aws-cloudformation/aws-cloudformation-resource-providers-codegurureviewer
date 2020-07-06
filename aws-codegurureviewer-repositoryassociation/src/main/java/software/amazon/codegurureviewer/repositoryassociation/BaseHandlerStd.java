package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.NotFoundException;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  protected final static int MAX_STABILIZE_ATTEMPTS = 5;
  protected final static int STABILIZE_SLEEP_TIME_MS = 7000;


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
}
