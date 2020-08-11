package com.oxygenxml.translation.ui.worker;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.oxygenxml.translation.support.core.ChangePackageGenerator;
import com.oxygenxml.translation.support.core.resource.ResourceFactory;
import com.oxygenxml.translation.support.storage.ResourceInfo;
import com.oxygenxml.translation.support.util.PackageGeneratorUtil;

/**
 * Collects the changed packages and creates the ZIP.
 * 
 * @author alex_jitianu
 */
public class GenerateChangePackageWorker extends AbstractWorker<File> {
  /**
   * Root map. Entry point for collecting changed resources.
   */
  private URL rootMap;
  /**
   * Destination zip file.
   */
  private File packageLocation;
  /**
   * A list of changed files that weren't copied in the package.
   */
  private List<URL> filesNotCopied;

  /**
   * Constructor.
   * 
   * @param rootMap Root map. Entry point for collecting changed resources.
   * @param packageLocation Destination zip file.
   */
  public GenerateChangePackageWorker(URL rootMap, File packageLocation) {
    this.rootMap = rootMap;
    this.packageLocation = packageLocation;
  }

  @Override
  protected File doInBackground() throws Exception {
    ChangePackageGenerator packageBuilder = new ChangePackageGenerator(listeners);
    List<ResourceInfo> modifiedResources = packageBuilder.collectModifiedResources(
        ResourceFactory.getInstance().getResource(rootMap));
    
    filesNotCopied = PackageGeneratorUtil.zipModifiedResources(rootMap, listeners, packageLocation, modifiedResources);
    
    return packageLocation;
  }
  
  /**
   * @return A list of changed files that weren't copied in the package.
   */
  public List<URL> getFilesNotCopied() {
    return filesNotCopied;
  }
}
