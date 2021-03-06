package bjoern.input.common;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bjoern.input.common.outputModules.CSV.CSVOutputModule;
import bjoern.input.radare.CommandLineInterface;

/**
 * Exporters extract information from binaries and make it available for later
 * import into the graph database. To achieve this, an input module is employed
 * to create logical objects (see package `structures`) from input binaries. An
 * output module subsequently makes logical objects available in an output
 * format.
 */

public abstract class Exporter
{
	protected abstract void initialize();

	protected abstract void export() throws IOException;

	protected InputModule inputModule;
	protected CSVOutputModule outputModule;
	protected CommandLineInterface cmdLine;

	private static final Logger logger = LoggerFactory
			.getLogger(Exporter.class);

	public Exporter()
	{
		initialize();
	}

	public void run(String[] args)
	{

		parseCommandLine(args);
		String binaryFilename = cmdLine.getBinaryFilename();
		String projectFilename = cmdLine.getProjectFilename();
		String outputDir = cmdLine.getOutputDir();

		tryToExport(binaryFilename, outputDir, projectFilename);
	}

	protected void parseCommandLine(String[] args)
	{
		try
		{
			cmdLine.parseCommandLine(args);
		} catch (RuntimeException | ParseException e)
		{
			printHelpAndTerminate(e);
		}
	}

	private void printHelpAndTerminate(Exception e)
	{
		System.err.println(e.getMessage());
		cmdLine.printHelp();
		System.exit(1);
	}

	private void printExceptionAndTerminate(IOException e)
	{
		System.err.println(e.getMessage());
		System.exit(1);
	}

	public void tryToExport(String binaryFilename, String outputDir, String projectFilename)
	{
		try
		{
			export(binaryFilename, outputDir, projectFilename);
		} catch (IOException e)
		{
			printExceptionAndTerminate(e);
		}
	}

	public void export(String binaryFilename, String outputDir, String projectFilename)
			throws IOException
	{

		logger.info("Exporting: {}", binaryFilename);

		inputModule.initialize(binaryFilename, projectFilename);
		outputModule.initialize(outputDir);
		export();
		outputModule.finish();
		inputModule.finish(outputDir);
	}

}
