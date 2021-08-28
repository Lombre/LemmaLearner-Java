package Configurations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Configurations implements ParsingConfigurations,
									   LearningConfigations, 
									   DatabaseConfigurations, 
									   LemmatizationConfigurations {
	
	
	private final Map<String, String> configurationKeyToValue;

	private final String CONFIG_LOCATION = "config.txt";
	
	private final String KEY_VALUE_DELIMITER_IN_CONFIG_FILE = "=";
	
	
	public Configurations() {
		configurationKeyToValue = loadConfigs(CONFIG_LOCATION);
		save(CONFIG_LOCATION);
	}
	
	protected Configurations(String rawConfig) {
		configurationKeyToValue = null;
	}

	private Map<String, String> loadConfigs(String fileLocation) {
		String rawConfiguration = loadRawConfigurations(fileLocation);	
		
		List<String> configurationLines = getConfigurationsLines(rawConfiguration);
		
		final var configurations = new TreeMap<String, String>();
		
		for (String configurationLine : configurationLines) {
			addConfigurationLineToConfiguration(configurations, configurationLine);
		}		
		
		return configurations;
	}

	private void addConfigurationLineToConfiguration(final Map<String, String> configurations, String configurationLine) throws Error {
		String[] configurationPair = configurationLine.split(KEY_VALUE_DELIMITER_IN_CONFIG_FILE);
		if (configurationPair.length != 2) 
			throw new Error("Exactly one equal sign is required for each line in the configuration file. See the line: " + configurationLine);
		String rawKey = configurationPair[0].trim();
		String rawValue = configurationPair[1].trim();
		if (configurations.containsKey(rawKey)) 
			throw new Error("They configuration file is not allowed to contain the same key twice. Second occurence in line: " + configurationLine);
		configurations.put(rawKey, rawValue);
	}

	private List<String> getConfigurationsLines(String rawConfiguration) {
		List<String> configurationLines = new ArrayList<String>(Arrays.asList(rawConfiguration.split("\n")));
		
		final char commentLineDenoter = '#';
		configurationLines = configurationLines.stream().map(line -> line.strip()).collect(Collectors.toList());
		//All empty lines and comments should be removed.
		configurationLines.removeIf(line -> line.length() == 0 || line.charAt(0) == commentLineDenoter);
		return configurationLines;
	}

	private String loadRawConfigurations(String fileLocation) throws Error {
		String rawConfiguration;
		try {
			rawConfiguration = Files.readString(Path.of(fileLocation),  StandardCharsets.UTF_8);
		} catch (IOException e) {			
			e.printStackTrace();
			throw new Error("Error when trying to load the configuration file. It should be located at: " + Path.of(fileLocation).toAbsolutePath().toString());
		}
		return rawConfiguration;
	}


	private String getConfigurationValue(final String configurationKey) throws Error {
		if (!configurationKeyToValue.containsKey(configurationKey)) {
			throw new Error("The configuration for \"" + configurationKey + "\" is not found in the configuration file.");
		} else {
			return configurationKeyToValue.get(configurationKey);
		}
	}
	
	
	private void save(String fileLocation) {
		String rawConfigurationToWrite = "";
		for (String configurationKey : configurationKeyToValue.keySet()) {
			String configurationValue = configurationKeyToValue.get(configurationKey);
			rawConfigurationToWrite += configurationKey + " " + KEY_VALUE_DELIMITER_IN_CONFIG_FILE + " " + configurationValue + "\n\n";
		}
		try {
			Files.writeString(Path.of(CONFIG_LOCATION), rawConfigurationToWrite);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private <E> E getConfigurationWithType(String configurationKey, Function<String, E> rawValueToType) {
		String rawValue = getConfigurationString(configurationKey);
		E result = rawValueToType.apply(rawValue);
		return result;
	}

	private String getConfigurationString(final String configurationKey) {
		return getConfigurationValue(configurationKey).toLowerCase();
	}
	
	private boolean getConfigurationBoolean(String configurationKey) {
		return getConfigurationWithType(configurationKey, (rawValue) -> Boolean.parseBoolean(rawValue));
	}

	private int getConfigurationInt(String configurationKey) {
		return getConfigurationWithType(configurationKey, (rawValue) -> Integer.parseInt(rawValue));
	}
	
	private double getConfigurationDouble(String configurationKey) {
		return getConfigurationWithType(configurationKey, (rawValue) -> Double.parseDouble(rawValue));
	}
	
	public boolean shouldLoadSavedTexts() {
		return getConfigurationBoolean("ShouldLoadSavedTexts");
	}

	public boolean shouldPrintText() {
		return getConfigurationBoolean("ShouldPrintText");
	}

	public double getScoreExponent() {
		return getConfigurationDouble("ScoreExponent");
	}
	
	public int getMaxTimesLemmaShouldBeLearned() {
		return getConfigurationInt("MaxTimesLemmaShouldBeLearned");
	}

	public int getMaxNumberOfSentencesToLearn() {
		return getConfigurationInt("MaxNumberOfSentencesToLearn");
	}

	public boolean shouldConjugationsBeScored() {
		return getConfigurationBoolean("ShouldConjugationsBeScored");
	}
	
	public String getLanguage() {
		return getConfigurationString("Language");
	}

	public boolean shouldSaveTexts() {
		return getConfigurationBoolean("ShouldSaveTexts");
	}
	
}
