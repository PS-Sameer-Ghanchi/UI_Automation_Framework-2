package Generator;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

public class StepDefGenerator {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        readExcel(scanner);
    }

    private static void readExcel(Scanner scanner) throws IOException {
        String userDirectory = System.getProperty("user.dir");
        String excelDirectory = userDirectory + "\\src\\test\\resources\\TestData\\";
        String defaultExcelPath = excelDirectory + "StepDefGenerator.xlsx";
        String excelFilePath = defaultExcelPath;

        // Check if the default Excel file exists
        File defaultExcelFile = new File(defaultExcelPath);
        if (!defaultExcelFile.exists()) {
            System.out.println("Default Excel file not found.");
            System.out.print("Enter the name of the Excel file you want to read: ");
            String excelName = scanner.nextLine();
            excelFilePath = excelDirectory + excelName + ".xlsx";
        }

        Fillo fillo = new Fillo();
        Connection connection = null;

        try {
            connection = fillo.getConnection(excelFilePath);

            String query = "SELECT * FROM Sheet1";

            Recordset recordset = connection.executeQuery(query);

            while (recordset.next()) {
                String featureFileName = recordset.getField("Feature File");
                String pageClassName = recordset.getField("PageObject File");
                String stepDefFileName = recordset.getField("StepDefinition File");

                generateStepDefinitionFile(featureFileName, pageClassName, stepDefFileName, scanner);
            }
        } catch (FilloException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void generateStepDefinitionFile(String featureFileName, String pageClassName, String stepDefFileName,
            Scanner scanner) throws IOException {
        String userDir = System.getProperty("user.dir");

        // Feature file path (relative to the project directory)
        String featureFilePath = userDir + "\\src\\test\\resources\\features\\" + featureFileName + ".feature";

        // Page class path (relative to the project directory)
        String pageClassPath = userDir + "\\src\\test\\java\\bdd\\pageObjects\\" + pageClassName + ".java";

        // Step definition file path (relative to the project directory)
        String stepDefFilePath = userDir + "\\src\\test\\java\\bdd\\StepDefinitions\\" + stepDefFileName + ".java";

        // Read feature file
        Path path = Paths.get(featureFilePath);
        List<String> featureLines = Files.readAllLines(path);

        // Read page class file
        Path pagePath = Paths.get(pageClassPath);
        List<String> pageLines = Files.readAllLines(pagePath);

        Path stepDefPath = Paths.get(stepDefFilePath);
        if (Files.exists(stepDefPath)) {
            System.err.println(
                    "Error: A file with the name '" + stepDefFileName + "' already exists in the target folder.");
            return;
        }

        // Get methods from page class
        List<Pair<String, String>> pageMethods = extractMethodsWithSignatures(pageLines);

        List<String> stepDefinitions = new ArrayList<>();

        // Regex to match Given, When, Then, And, But
        Pattern pattern = Pattern.compile("^(given|when|then|and|but)\\s+(.+)", Pattern.CASE_INSENSITIVE);

        // Creating an object of the page class
        String pageObject = pageClassName.toLowerCase() + "Obj";
        String pageClassObjectCreation = String.format("%s %s = new %s(driver);\n    ", pageClassName, pageObject,
                pageClassName);

        // Initialize HashSet to keep track of already called methods
        Set<String> calledMethods = new HashSet<>();

        // Initialize HashMap to keep track of the user's chosen step for each method
        Map<String, List<Integer>> chosenStepForMethod = new HashMap<>();

        Map<String, List<String[]>> methodMatches = new HashMap<>();

        Set<String> generatedSteps = new HashSet<>();

        // Initialize a set to keep track of generated step definitions
        Set<String> generatedStepDefinitions = new HashSet<>();

        // Iterate over each line in the feature file to find Given/When/Then
        // annotations
        for (String line : featureLines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
                String step = matcher.group(1);
                String definition = matcher.group(2);

                // Create a unique key for the step
                String uniqueStepKey = step + definition;

                // Check if this step has already been generated
                if (!generatedStepDefinitions.contains(uniqueStepKey)) {
                    generatedStepDefinitions.add(uniqueStepKey); // Add to set to prevent future duplicates

                    // Attempt to find a matching method in the page class for the current
                    // definition
                    String methodSignature = findMethod(pageMethods, definition, generatedSteps);

                    // If a matching method is found, add it to the methodMatches map
                    if (methodSignature != null) {
                        methodMatches.computeIfAbsent(methodSignature, k -> new ArrayList<>())
                                .add(new String[] { step, definition });
                    } else {
                        // If no matching method is found, add a placeholder method call for this step
                        methodMatches.computeIfAbsent("placeholder", k -> new ArrayList<>())
                                .add(new String[] { step, definition });
                    }
                }
            }
        }
        // Iterate over methodMatches to handle multiple matches and user choice
        methodMatches.forEach((step, definitions) -> {
            for (String[] definitionArray : definitions) {
                String stepAnnotation = definitionArray[0]; // Use the step annotation from the feature file
                String definition = definitionArray[1];

                String methodName = definition.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", "_").toLowerCase();

                boolean isScenarioOutlineStep = definition.contains("<") && definition.contains(">");

                if (isScenarioOutlineStep) {
                    // Correctly format the placeholders without additional quotes
                    String parameterizedDefinition = definition.replaceAll("<", "{").replaceAll(">", "}");

                    // Extracting parameters and preparing them for method signature
                    List<String> parameterMatches = new ArrayList<>();
                    Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(parameterizedDefinition);
                    while (matcher.find()) {
                        String paramName = matcher.group(1); // Add the placeholder name found
                        parameterMatches.add(paramName);
                        // Replace the placeholder name with "string"
                        parameterizedDefinition = parameterizedDefinition.replace("{" + paramName + "}", "{string}");
                        parameterizedDefinition = parameterizedDefinition.replace("\"", "");
                    }
                    // Constructing parameters for method signature
                    String parameters = parameterMatches.stream()
                            .map(param -> "String " + param)
                            .collect(Collectors.joining(", "));

                    // Preparing the method signature with parameters
                    String formattedMethod = String.format("@%s(\"%s\")\npublic void %s(%s) {\n", stepAnnotation,
                            parameterizedDefinition, methodName, parameters);

                    // Check if the step annotation is "Then" and if the definition contains "Should
                    // be"
                    if (stepAnnotation.equalsIgnoreCase("then") && definition.toLowerCase().contains("should be")) {
                        // Get the parameter name from the method signature
                        String expectedParameter = parameterMatches.get(0); // Assuming only one parameter
                        // Generate the assertion statement using the parameter

                        // Call the appropriate verification method from the page object class
                        String verificationMethod = findVerificationMethod(pageObject, definition, pageMethods);

                        if (verificationMethod != null) {
                            formattedMethod += String.format("    Assert.assertEquals(%s,%s.%s);\n", expectedParameter,
                                    pageObject,
                                    verificationMethod);
                        }
                    } else {
                        // String methodSignature = findMethod(pageMethods, definition, generatedSteps);
                        // System.out.println(methodSignature);
                        // formattedMethod += String.format(" // Implement Scenario Outline step\n");
                        String methodCall = findMethod(pageMethods, definition, generatedSteps);

                        // Extract only the parameter names without the "String" keyword
                        String parameterNames = Arrays.stream(parameters.split(", "))
                                .map(param -> param.split(" ")[1])
                                .collect(Collectors.joining(", "));

                        // Remove parameters from methodCall
                        String methodNameOnly = methodCall.replaceAll("\\(.*\\)", "");

                        formattedMethod += String.format("    %s.%s(%s);\n", pageObject, methodNameOnly,
                                parameterNames);
                    }

                    formattedMethod += "}\n";

                    stepDefinitions.add(formattedMethod);
                }
            }
        });

        // Ask the user to choose which step to bind the method call for methods that
        // match multiple steps
        methodMatches.forEach((methodSignature, matches) -> {
            if (!methodSignature.equals("placeholder") && matches.size() > 1) {
                System.out.println("The method '" + methodSignature
                        + "' matches multiple steps. Please select the step number(s) in which you want to call it (comma-separated):");
                int stepIndex = 1;
                for (String[] match : matches) {
                    System.out.println(stepIndex++ + ". " + match[0].toUpperCase() + " " + match[1]);
                }
                String stepChoicesStr = scanner.nextLine(); // Read user input
                String[] stepChoices = stepChoicesStr.split(","); // Split user input by comma
                List<Integer> chosenStepIndices = new ArrayList<>();
                for (String choice : stepChoices) {
                    try {
                        int stepChoice = Integer.parseInt(choice.trim());
                        if (stepChoice >= 1 && stepChoice <= matches.size()) {
                            chosenStepIndices.add(stepChoice - 1);
                        } else {
                            System.out.println("Invalid choice: " + choice);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice: " + choice);
                    }
                }

                // Store the chosen step indices for the method signature
                chosenStepForMethod.put(methodSignature, chosenStepIndices);
            }
        });

        // Generate step definitions based on user choices and method matches
        Set<String> generatedMethods = new HashSet<>();
        for (Map.Entry<String, List<String[]>> entry : methodMatches.entrySet()) {
            String methodSignature = entry.getKey();
            List<String[]> matches = entry.getValue();
            List<Integer> chosenIndices = chosenStepForMethod.get(methodSignature); // Get the chosen step indices for
                                                                                    // this method signature

            if (!methodSignature.equals("placeholder")) {
                for (int i = 0; i < matches.size(); i++) {
                    String[] match = matches.get(i);
                    String step = match[0];
                    String definition = match[1];
                    String methodName = definition.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", "_")
                            .toLowerCase();

                    if (chosenIndices == null || chosenIndices.contains(i)) {
                        if (!generatedMethods.contains(methodSignature)) {
                            // Extract the parameters from methodSignature
                            int openingParenIndex = methodSignature.indexOf("(");
                            if (openingParenIndex != -1) {
                                String parameters = methodSignature.substring(openingParenIndex + 1,
                                        methodSignature.indexOf(")"));

                                // Extract the method name from methodSignature
                                String methodNameParam = methodSignature.substring(0, openingParenIndex);

                                // Check if there are parameters
                                if (!parameters.isEmpty()) {
                                    // Split the parameters string by comma to get individual parameter names
                                    String[] parameterNames = parameters.trim().split(", ");

                                    // Check if the step contains placeholders
                                    if (definition.contains("<") && definition.contains(">")) {
                                        // Use placeholders directly in the method call
                                        StringBuilder arguments = new StringBuilder();
                                        for (String parameter : parameterNames) {
                                            String parameterName = parameter.split(" ")[1];
                                            arguments.append(parameterName).append(", ");
                                        }
                                        if (arguments.length() > 0) {
                                            arguments.delete(arguments.length() - 2, arguments.length());
                                        }
                                        String modifiedMethodCall = String.format("%s(%s)", methodNameParam, arguments);

                                        if (definition.contains("<")) {
                                            continue;
                                        } else {
                                            // Generate step definition
                                            stepDefinitions.add(
                                                    String.format("@%s(\"%s\")\npublic void %s(%s) {\n    %s.%s;\n}\n",
                                                            step.toLowerCase(), escapeSpecialCharsInRegex(definition),
                                                            methodName,
                                                            parameters, pageObject, modifiedMethodCall));
                                        }

                                    } else {
                                        // Use prop.getProperty for parameters in the method call
                                        StringBuilder arguments = new StringBuilder();
                                        for (String parameter : parameterNames) {
                                            String parameterName = parameter.split(" ")[1];
                                            arguments.append("prop.getProperty(\"").append(parameterName)
                                                    .append("\"), ");
                                        }
                                        if (arguments.length() > 0) {
                                            arguments.delete(arguments.length() - 2, arguments.length());
                                        }
                                        String modifiedMethodCall = String.format("%s(%s)", methodNameParam, arguments);

                                        // Generate step definition
                                        stepDefinitions.add(String.format(
                                                "@%s(\"%s\")\npublic void %s() {\n    %s.%s;\n}\n",
                                                step.toLowerCase(), escapeSpecialCharsInRegex(definition), methodName,
                                                pageObject, modifiedMethodCall));
                                    }
                                } else {

                                    // No parameters case
                                    String modifiedMethodCall = String.format("%s()", methodNameParam);
                                    if(definition.contains("<")){
                                        continue;
                                    }else{
                                        // Generate step definition
                                        stepDefinitions.add(String.format(
                                            "@%s(\"%s\")\npublic void %s() {\n    %s.%s;\n}\n",
                                            step.toLowerCase(), escapeSpecialCharsInRegex(definition), methodName,
                                            pageObject, modifiedMethodCall));
                                        }
                                }
                            } else {
                                System.err.println(
                                        "Opening parenthesis not found in method signature: " + methodSignature);
                            }

                            generatedMethods.add(methodSignature);
                        }
                    } else {
                        // Generate placeholder for other steps
                        if (chosenIndices.contains(i)) {
                            stepDefinitions.add(
                                    String.format("@%s(\"%s\")\npublic void %s() {\n    // Placeholder for %s\n}\n",
                                            step.toLowerCase(), definition, methodName.toLowerCase(), definition));
                            calledMethods.add(methodSignature);
                        }
                    }
                }
            } else {
                // Handle placeholders
                for (String[] match : matches) {
                    String step = match[0];
                    String definition = match[1];
                    String methodName = definition.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", "_")
                            .toLowerCase();
                    if (definition.contains("<") && definition.contains(">")) {
                        continue;
                    }
                    stepDefinitions
                            .add(String.format("@%s(\"^%s\")\npublic void %s() {\n    // Placeholder for %s\n}\n",
                                    step.toLowerCase(), escapeSpecialCharsInRegex(definition), methodName, definition));
                }
            }
        }

        if (!stepDefinitions.isEmpty()) {
            // Write step definition file
            try (BufferedWriter writer = Files.newBufferedWriter(stepDefPath)) {
                writer.write("package bdd.StepDefinitions;\n\n");
                writer.write("import io.cucumber.java.en.*;\n");
                writer.write("import bdd.pageObjects.*;\n\n");
                writer.write("import org.junit.Assert;\n");
                writer.write("import java.io.IOException;\n");
                writer.write("import java.util.Properties;\n");
                writer.write("import org.openqa.selenium.WebDriver;\n");
                writer.write("import base.BaseClass;\n"); // Assuming BaseClass is in package bdd.base
                writer.write("public class " + stepDefFileName + " {\n\n");

                // Write properties and the page class object creation line
                writer.write("public WebDriver driver;\n");
                writer.write("Properties prop;\n");
                writer.write("    " + pageClassName + " " + pageObject + ";\n");

                // Constructor
                writer.write("    public " + stepDefFileName + "() throws IOException{\n");
                writer.write("BaseClass.startDriver();\n");
                writer.write("        this.driver = BaseClass.getDriver();\n");
                writer.write("        this.prop = BaseClass.getProperties();\n");
                writer.write("        " + pageObject + " = new " + pageClassName + "(driver);\n");
                writer.write("    }\n\n");

                // Write the step definitions
                for (String def : stepDefinitions) {
                    writer.write("    " + def.substring(0, 2).toUpperCase() + def.substring(2)); // Capitalize first
                                                                                                 // letter after @
                }
                writer.write("}\n");

                System.out.println("Step definition file generated: " + stepDefFileName + ".java");
                System.out.println("Location: " + stepDefPath);
            }
        } else {
            System.out.println("No step definitions were found in the feature file.");
        }
    }

    private List<String> availableStrings;

    public void SimpleMatcher(List<String> availableStrings) {
        this.availableStrings = availableStrings;
    }

    // Helper method to split camel case strings into separate words
    private static List<String> splitCamelCase(String s) {
        return Arrays.asList(s.split("(?<=\\p{Ll})(?=\\p{Lu})"));
    }

    private static String escapeSpecialCharsInRegex(String input) {
        return input.replaceAll("([{}()\\[\\].+*?^$\\\\|])", "\\\\$1").replaceAll("\"", "\\\\\"");
    }

    public static List<Pair<String, String>> extractMethodsWithSignatures(List<String> lines) {
        List<Pair<String, String>> methods = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            // Check if the line starts with public, private, or protected followed by at
            // least one space
            if (line.startsWith("public ")) {
                int indexOfOpeningParenthesis = line.indexOf("(");
                int indexOfClosingParenthesis = line.indexOf(")");
                int indexOfSpaceBeforeMethodName = line.lastIndexOf(" ", indexOfOpeningParenthesis);

                if (indexOfOpeningParenthesis != -1 && indexOfClosingParenthesis != -1
                        && indexOfOpeningParenthesis < indexOfClosingParenthesis
                        && indexOfSpaceBeforeMethodName != -1) {
                    // Extract the method signature including parameters
                    String methodSignature = line
                            .substring(indexOfSpaceBeforeMethodName + 1, indexOfClosingParenthesis + 1).trim();
                    // Extract only the method name
                    String methodName = methodSignature.substring(0, methodSignature.indexOf("(")).trim();
                    methods.add(new Pair<>(methodName, methodSignature));
                    // System.out.println(methodSignature);
                }
            }
        }
        return methods;
    }

    private static String findVerificationMethod(String pageObject, String definition,
            List<Pair<String, String>> pageMethods) {
        for (Pair<String, String> pair : pageMethods) {

            // System.out.println(pageMethods);
            String methodName = pair.getKey(); // Assuming the method name is stored in the key
            String methodNameWithParam = pair.getValue();

            List<String> strings = new ArrayList<>();
            strings.add(methodNameWithParam);
            // System.out.println(methodNameWithParam);
            String[] inputWords = definition.toLowerCase().split("\\s+"); // Split the input string into words
            for (String possibleMatch : strings) {
                List<String> splitWords = splitCamelCase(methodNameWithParam); // Split the available string into words
                                                                               // based on camel case
                int matchCount = 0;
                for (String word : inputWords) {
                    for (String splitWord : splitWords) {
                        if (splitWord.toLowerCase().contentEquals(word)) { // Check if the split word contains the input
                                                                           // word
                            matchCount++;
                            if (matchCount >= 2) { // If two or more words match, return this string
                                return possibleMatch;
                            }
                        }
                    }
                }
            }

        }
        return null;

    }

    private static String findMethod(List<Pair<String, String>> methods, String definition,
            Set<String> generatedSteps) {
        for (Pair<String, String> methodPair : methods) {
            String methodName = methodPair.getKey();
            // System.out.println(methodName);
            List<String> splitWords = splitCamelCase(methodName);
            int matchCount = 0;
            for (String methodWord : splitWords) {
                // if (definition.contains("<") || generatedSteps.contains(definition)) {
                // // System.out.println(definition);
                // continue;
                // }
                if (definition.toLowerCase().contains(methodWord.toLowerCase())) {
                    matchCount++;
                }
            }

            // If more than two words match, return the method name
            if (matchCount > 2) {
                return methodPair.getValue(); // Return the full method signature including parameters
            }
            // Use containsIgnoreCase for case-insensitive comparison
            // if (definition.toLowerCase().contains(methodName.toLowerCase())) {
            // return methodPair.getValue(); // Return the full method signature including
            // parameters
            // }

        }
        return null;
    }

}
