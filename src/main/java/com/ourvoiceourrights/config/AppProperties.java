package com.ourvoiceourrights.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

	private String dataGovBaseUrl = "https://api.data.gov.in/resource";
	private String dataGovResourceId = "c8ef53ea-d686-4f02-84a1-f4584790bba6"; // placeholder resource id
	private String ingestionScheduleCron = "0 0 */6 * * *";
	private int ingestLimit = 100;
	private Ingestion ingestion = new Ingestion();
	private Cache cache = new Cache();
	private Geo geo = new Geo();
	private RateLimit rateLimit = new RateLimit();

	public String getDataGovBaseUrl() {
		return dataGovBaseUrl;
	}

	public void setDataGovBaseUrl(String dataGovBaseUrl) {
		this.dataGovBaseUrl = dataGovBaseUrl;
	}

	public String getDataGovResourceId() {
		return dataGovResourceId;
	}

	public void setDataGovResourceId(String dataGovResourceId) {
		this.dataGovResourceId = dataGovResourceId;
	}

	public String getIngestionScheduleCron() {
		return ingestionScheduleCron;
	}

	public void setIngestionScheduleCron(String ingestionScheduleCron) {
		this.ingestionScheduleCron = ingestionScheduleCron;
	}

	public int getIngestLimit() {
		return ingestLimit;
	}

	public void setIngestLimit(int ingestLimit) {
		this.ingestLimit = ingestLimit;
	}

	public Ingestion getIngestion() {
		return ingestion;
	}

	public void setIngestion(Ingestion ingestion) {
		this.ingestion = ingestion;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Geo getGeo() {
		return geo;
	}

	public void setGeo(Geo geo) {
		this.geo = geo;
	}

	public RateLimit getRateLimit() {
		return rateLimit;
	}

	public void setRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}

	@Validated
	public static class Ingestion {

		@NotEmpty
		private List<String> defaultStates = List.of("ANDHRA PRADESH", "KARNATAKA", "ODISHA");

		@Min(1)
		@Max(10)
		private int rollingFinancialYears = 3;

		@Min(1)
		@Max(1000)
		private int maxFetchLimit = 500;

		@Min(0)
		private int paginationStep = 100;

		@Min(1)
		@Max(10)
		private int maxRetryAttempts = 5;

		@Min(100)
		private long retryInitialIntervalMs = 200;

	@jakarta.validation.constraints.DecimalMin("1.0")
	private double retryMultiplier = 2.0;

		public List<String> getDefaultStates() {
			return defaultStates;
		}

		public void setDefaultStates(List<String> defaultStates) {
			this.defaultStates = defaultStates;
		}

		public int getRollingFinancialYears() {
			return rollingFinancialYears;
		}

		public void setRollingFinancialYears(int rollingFinancialYears) {
			this.rollingFinancialYears = rollingFinancialYears;
		}

		public int getMaxFetchLimit() {
			return maxFetchLimit;
		}

		public void setMaxFetchLimit(int maxFetchLimit) {
			this.maxFetchLimit = maxFetchLimit;
		}

		public int getPaginationStep() {
			return paginationStep;
		}

		public void setPaginationStep(int paginationStep) {
			this.paginationStep = paginationStep;
		}

		public int getMaxRetryAttempts() {
			return maxRetryAttempts;
		}

		public void setMaxRetryAttempts(int maxRetryAttempts) {
			this.maxRetryAttempts = maxRetryAttempts;
		}

		public long getRetryInitialIntervalMs() {
			return retryInitialIntervalMs;
		}

		public void setRetryInitialIntervalMs(long retryInitialIntervalMs) {
			this.retryInitialIntervalMs = retryInitialIntervalMs;
		}

		public double getRetryMultiplier() {
			return retryMultiplier;
		}

		public void setRetryMultiplier(double retryMultiplier) {
			this.retryMultiplier = retryMultiplier;
		}
	}

	@Validated
	public static class Cache {

		@NotBlank
		private String providerEnv = "caffeine";

		private Duration statesTtl = Duration.ofHours(12);
		private Duration districtsTtl = Duration.ofHours(6);
		private Duration latestPerformanceTtl = Duration.ofMinutes(30);
		private Duration historyTtl = Duration.ofMinutes(10);

		public String getProviderEnv() {
			return providerEnv;
		}

		public void setProviderEnv(String providerEnv) {
			this.providerEnv = providerEnv;
		}

		public Duration getStatesTtl() {
			return statesTtl;
		}

		public void setStatesTtl(Duration statesTtl) {
			this.statesTtl = statesTtl;
		}

		public Duration getDistrictsTtl() {
			return districtsTtl;
		}

		public void setDistrictsTtl(Duration districtsTtl) {
			this.districtsTtl = districtsTtl;
		}

		public Duration getLatestPerformanceTtl() {
			return latestPerformanceTtl;
		}

		public void setLatestPerformanceTtl(Duration latestPerformanceTtl) {
			this.latestPerformanceTtl = latestPerformanceTtl;
		}

		public Duration getHistoryTtl() {
			return historyTtl;
		}

		public void setHistoryTtl(Duration historyTtl) {
			this.historyTtl = historyTtl;
		}
	}

	@Validated
	public static class Geo {

		private String geoIpDatabasePath = "";
		private String reverseGeocodeBaseUrl = "";
		private Duration reverseGeocodeTimeout = Duration.ofSeconds(3);
		private Duration geoIpCacheTtl = Duration.ofHours(6);

		public String getGeoIpDatabasePath() {
			return geoIpDatabasePath;
		}

		public void setGeoIpDatabasePath(String geoIpDatabasePath) {
			this.geoIpDatabasePath = geoIpDatabasePath;
		}

		public String getReverseGeocodeBaseUrl() {
			return reverseGeocodeBaseUrl;
		}

		public void setReverseGeocodeBaseUrl(String reverseGeocodeBaseUrl) {
			this.reverseGeocodeBaseUrl = reverseGeocodeBaseUrl;
		}

		public Duration getReverseGeocodeTimeout() {
			return reverseGeocodeTimeout;
		}

		public void setReverseGeocodeTimeout(Duration reverseGeocodeTimeout) {
			this.reverseGeocodeTimeout = reverseGeocodeTimeout;
		}

		public Duration getGeoIpCacheTtl() {
			return geoIpCacheTtl;
		}

		public void setGeoIpCacheTtl(Duration geoIpCacheTtl) {
			this.geoIpCacheTtl = geoIpCacheTtl;
		}
	}

	@Validated
	public static class RateLimit {

		@Min(1)
		private int requestsPerMinute = 120;

		public int getRequestsPerMinute() {
			return requestsPerMinute;
		}

		public void setRequestsPerMinute(int requestsPerMinute) {
			this.requestsPerMinute = requestsPerMinute;
		}
	}
}

