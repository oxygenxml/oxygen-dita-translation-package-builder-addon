package com.oxygenxml.translation.ui;
/**
 * An interface used for accessing the key values from the "i18n/translation.xml" file.
 */
public interface Tags /*NOSONAR*/ {
  
  /**
   * Progress message. A file is copied into the temporary package directory.
   */
  public static final String COPY_TO_PACKAGE_DIR = "Copy_to_package_dir";
  /**
   * Progress message. A file is being analyzed to see if it has changes compared to the milestone version.
   */
  public static final String ANALYZE_FOR_CHANGES = "Analyze_for_changes";
  
  /**
   * Progress message. A file is being added to the translation package.
   */
  public static final String ADD_TO_PACKAGE = "Add_to_package";
  /**
   * Progress message. A file is unpacked from the translation package.
   */
  public static final String UNPACK_FILE = "Unpack_file";
  
  public final String TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME = "translation_package_builder_plugin_name";
  
  /**
   * en: Generate Milestone
   */
  public final String GENERATE_MILESTONE = "Generate_milestone";
  /**
   * Tooltip for the action that generates a milestone.
   */
  public final String GENERATE_MILESTONE_TOOLTIP = "Generate_milestone_tooltip";
  
  /**
   * Name of the option that generates the archive with modified files.
   * 
   * en: Create modified files package
   */
  public final String CREATE_MODIFIED_FILES_PACKAGE = "create_modified_files_package";
  /**
   * Tooltip for the action that creates the package for translation.
   * 
   * en: Creates a package with all the files that were modified (since the last generation of a translation_builder_milestone.xml file) at a chosen location.
   */
  public final String CREATE_PACKAGE_TOOLTIP = "Create_package_tooltip";
  /**
   * The name of the action that applies a translation package.
   * 
   * en: Apply Package
   */
  public final String APPLY_PACKAGE = "Apply_package";
  /**
   * Tooltip for the action that applies a translation package.
   * 
   * en: Applies a chosen archive over the root directory of the current ditamap.
   */
  public final String APPLY_PACKAGE_TOOLTIP ="Apply_package_tooltip";
  
  /**
   * Notification presented to the user after a milestone file is generated.
   * 
   * en: Milestone created at: {0}
   */
  public final String MILESTONE_GENERATED = "Milestone_generated";
  
  /**
   * en: Milestone creation failed because of:
   */
  public final String MILESTONE_CREATION_FAILED_BECAUSE = "milestone_creation_failed_because";
  
  /**
   * Title of progress dialog shown when a milestone is generating.
   * 
   * en: Generating Milestone
   */
  public final String GENERATING_MILESTONE = "Generating_milestone";
  
  /**
   * en: Package location
   */
  public final String PACKAGE_LOCATION = "Package_location";
  /**
   * File type in file chooser. 
   * 
   * en: Zip Files
   */
  public final String ZIP_FILES = "Zip_files";
  /**
   * en: Milestone Missing
   */
  public final String MILESTONE_MISSING = "Milestone_missing";
  
  /**
   * en: Create new milestone? \n {0}
   */
  public final String CREATE_NEW_MILESTONE = "Create_new_milestone";
  /**
   * Modified files generation, milestone is missing. We can generate 
   * a new milestone or archive an entire directory. 
   * 
   * en: Pack entire dir
   */
  public final String PACK_ENTIRE_DIR = "Pack_entire_dir";
  
  public final String YES_BUTTON = "yes_button";
  public final String NO_BUTTON = "no_button";
  
  /**
   * Progress dialog title when the files are packed into an archive.
   * 
   * en: Zipping directory
   */
  public final String CREATE_PACKAGE_ARCHIVE_TITLE = "Create_package_archive_title";
  
  /**
   * en: The directory was packed.
   */
  public final String DIRECTORY_WAS_PACKED = "directory_was_packed";
  
  /**
   * en: A package containing {0} file(s) was created.
   */
  public final String REPORT_NUMBER_OF_MODIFIED_FILES = "Report_number_of_modified_files";
  
  /**
   * Message presented when the translation package can't be created.
   * 
   * en: Could not create package. 
   */
  public final String FAILURE_CREATING_PACKAGE = "Failure_creating_package";
  /**
   * Message presented when the translation package can't be buiult because there are no changed since the milestone was created.
   * 
   * en: There are no changed files since the milestone created on: {0}
   */
  public final String NO_CHANGED_FILES = "No_changed_files";
  /**
   * An action failed.
   * 
   * en: Failed because of: 
   */
  public final String ACTION_FAILED = "action2_error_message";
  /**
   * Title of the progress dialog while collecting the modified resources.
   * 
   * en: Collect modified resources
   */
  public final String COLLECT_MODIFIED_RESOURCES = "Collecting_modified_resources";
  
  /**
   * Title of the dialog in which the user selects the translation package to apply.
   * 
   * en: Choose the translated package
   */
  public final String CHOOSE_TRANSLATION_PACKAGE = "Choose_translation_package";
  /**
   * en: Preview Changes
   */
  public final String PREVIEW_CHANGES = "preview_changes";
  
  /**
   * en: Do you want you to preview all the changes that will be made before applying them?
   */
  public final String PREVIEW_CHANGES_USER_QUESTION = "preview_changes_user_question";
  
  /**
   * en: Apply All
   */
  public final String APPLY_ALL = "apply_all";
  
  /**
   * en: Cancel
   */
  public final String CANCEL = "cancel";
  
  /**
   * Progress dialog title in apply package.
   * 
   * en: Opening package
   */
  public final String OPENING_PACKAGE = "Opening_package";
  /**
   * Preview dialog title in apply package.
   */
  public final String PREVIEW = "preview";
  
  /**
   * en: Failed to apply package because of: 
   */
  public final String FAILED_TO_APPLY_PACKAGE = "failed_to_apply_package";
  /**
   * Notification after a translation package was applied.
   * 
   * en: Package applied over the current map. The overridden files are: 
   */
  public final String APPLY_PACKAGE_REPORT = "Apply_package_report";
  /**
   * Title of the report dialog after a translation package was applied.
   * 
   * en: Updated files
   */
  public final String UPDATED_FILES = "Updated_files";
  /**
   * Error message when we didn't extract any files from the translation package.
   * 
   * en: The translation package didn't contain any file.
   */
  public final String NO_FILES_IN_PACKAGE = "No_files_in_package";
  /**
   * Apply.
   */
  public final String APPLY_BUTTON = "Apply";
  
  /**
   * Switch to tree view
   */
  public final String SWICH_TO_TREE_VIEW = "switch_to_tree_view";
  /**
   * Switch to list view.
   */
  public final String SWICH_TO_LIST_VIEW = "switch_to_list_view";
  
  /**
   * en: There are no selected files to apply.
   */
  public final String NO_SELECTED_FILES_TO_APPLY = "No_selected_files_to_apply";
  /**
   * en: Applying selected files.
   */
  public final String APPLYING_SELECTED_FILES = "Applying_selected_files";
  
  /**
   * Notification after a translation package was applied.
   * 
   * en: The translated files have been applied.
   */
  public final String TRANSLATED_FILES_APPLIED = "Translated_files_applied";
  /**
   * Message presented when the translated files couldn't be copied in the map directory.
   * 
   * en: Couldn't copy translated files because of: 
   */
  public final String COPY_TRANSLATED_FILES_ERROR_MESSAGE = "Copy_translated_files_error_message";
  /**
   * Information message presented when the user requested to see the differences between the new file version and the old, 
   * but the file is not supported.
   * 
   * en: The selected file is not supported by Oxygen XML Editor.
   */
  public final String FILE_TYPE_NOT_SUPPORTED = "File_type_not_supported";
  /**
   * Option to automatically select all presented files. The selected files are applied over their old versions.
   * 
   * en: Select all files
   */
  public final String SELECT_ALL_FILES = "Select_all_files";
  /**
   * Progress message while copying files.
   * 
   * en: Copy {0} to {1}
   */
  public final String COPY_FILE_TO = "Copy_file_to";
  
  /**
   * en: A report will be created at: a_date.
   */
  public final String XHTML_REPORT_LOCATION = "report_will_be_created";
  /**
   * An option in the generate translation package dialog.
   * 
   * en: Generate a report containing the relative paths of the modified resources.
   */
  public final String GENERATE_REPORT_TOOLTIP = "Generate_report_tooltip";
  /**
   * An option in the generate translation package dialog.
   * 
   * en: Generate a report containing the relative paths of the modified resources.
   */
  public final String GENERATE_REPORT = "Generate_report";
  
  /**
   * en: Last report was created on {0}
   */
  public final String REPORT_CREATION_TIME = "Report_creation_time";
  
  /**
   * Save
   */
  public final String SAVE = "save";
}
