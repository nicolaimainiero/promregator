package org.cloudfoundry.promregator.cfaccessor;

import org.cloudfoundry.client.v2.applications.ListApplicationsResponse;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsResponse;
import org.cloudfoundry.client.v2.spaces.GetSpaceSummaryResponse;
import org.cloudfoundry.client.v2.spaces.ListSpacesResponse;

import reactor.core.publisher.Mono;

public interface CFAccessor {

	Mono<ListOrganizationsResponse> retrieveOrgId(String orgName);

	Mono<ListSpacesResponse> retrieveSpaceId(String orgId, String spaceName);

	Mono<ListApplicationsResponse> retrieveApplicationId(String orgId, String spaceId, String applicationName);

	Mono<ListApplicationsResponse> retrieveAllApplicationIdsInSpace(String orgId, String spaceId);

	Mono<GetSpaceSummaryResponse> retrieveSpaceSummary(String spaceId);
}