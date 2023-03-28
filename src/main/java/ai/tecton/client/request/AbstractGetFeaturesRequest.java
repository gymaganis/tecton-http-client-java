package ai.tecton.client.request;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import ai.tecton.client.model.MetadataOption;
import ai.tecton.client.transport.TectonHttpClient.HttpMethod;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.*;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class for GetFeaturesRequest and GetFeaturesBatchRequest and extends AbstractTectonRequest
 */
public abstract class AbstractGetFeaturesRequest extends AbstractTectonRequest {

  private static final HttpMethod httpMethod = HttpMethod.POST;
  final Set<MetadataOption> metadataOptions;

  AbstractGetFeaturesRequest(
      String workspaceName,
      String featureServiceName,
      String endpoint,
      Set<MetadataOption> metadataOptions)
      throws TectonClientException {
    super(endpoint, httpMethod, workspaceName, featureServiceName);
    if (metadataOptions == null || metadataOptions.size() == 0) {
      this.metadataOptions = RequestConstants.DEFAULT_METADATA_OPTIONS;
    } else {
      this.metadataOptions = getMetadataOptions(metadataOptions);
    }
  }

  Set<MetadataOption> getMetadataOptions() {
    return this.metadataOptions;
  }

  static Set<MetadataOption> getMetadataOptions(Set<MetadataOption> metadataOptions) {
    Set<MetadataOption> finalMetadataOptionSet;
    if (metadataOptions.contains(MetadataOption.ALL)) {
      // Add everything except ALL and NONE from MetadataOption EnumSet
      finalMetadataOptionSet = RequestConstants.ALL_METADATA_OPTIONS;
    } else if (metadataOptions.contains(MetadataOption.NONE)) {
      finalMetadataOptionSet = RequestConstants.NONE_METADATA_OPTIONS;
    } else {
      finalMetadataOptionSet = metadataOptions;
    }
    finalMetadataOptionSet.addAll(
        RequestConstants.DEFAULT_METADATA_OPTIONS); // add default metadata options
    return finalMetadataOptionSet;
  }

  static void validateRequestParameters(GetFeaturesRequestData getFeaturesRequestData) {
    if (getFeaturesRequestData.isEmptyJoinKeyMap()
        && getFeaturesRequestData.isEmptyRequestContextMap()) {
      throw new TectonClientException(TectonErrorMessage.EMPTY_REQUEST_MAPS);
    }
  }

  @Retention(RUNTIME)
  @JsonQualifier
  public @interface SerializeNulls {
    JsonAdapter.Factory JSON_ADAPTER_FACTORY =
        new JsonAdapter.Factory() {
          @Nullable
          @Override
          public JsonAdapter<?> create(
              Type type, Set<? extends Annotation> annotations, Moshi moshi) {
            Set<? extends Annotation> nextAnnotations =
                Types.nextAnnotations(annotations, SerializeNulls.class);
            if (nextAnnotations == null) {
              return null;
            }
            return moshi.nextAdapter(this, type, nextAnnotations).serializeNulls();
          }
        };
  }
}
