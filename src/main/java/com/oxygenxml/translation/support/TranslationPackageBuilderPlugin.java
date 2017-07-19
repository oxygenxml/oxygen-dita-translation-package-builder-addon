package com.oxygenxml.translation.support;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * Workspace access plugin. 
 */
public class TranslationPackageBuilderPlugin extends Plugin {
  /**
   * The static plugin instance.
   */
  private static TranslationPackageBuilderPlugin instance = null;

  /**
   * Constructs the plugin.
   * 
   * @param descriptor The plugin descriptor
   */
  public TranslationPackageBuilderPlugin(PluginDescriptor descriptor) {
    super(descriptor);

    if (instance != null) {
      throw new IllegalStateException("Already instantiated!");
    }
    instance = this;
  }
  
  /**
   * Get the plugin instance.
   * 
   * @return the shared plugin instance.
   */
  public static TranslationPackageBuilderPlugin getInstance() {
    return instance;
  }
}