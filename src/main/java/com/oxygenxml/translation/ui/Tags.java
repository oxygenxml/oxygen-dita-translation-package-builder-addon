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
  
  public final String ACTION2_INFO_MESSAGE_EXCEPTION = "action2_infoMessage_exceptions";
  public final String ACTION2_NO_CHANGED_FILES_EXCEPTION = "action2_NoChangedFiles_exception";
  public final String ACTION2_ERROR_MESSAGE = "action2_error_message";
  public final String ACTION2_PACK_MODIFIED_PROGRESS_TITLE = "action2_packOnlyModified_progress_title";
  
  public final String ACTION3_CHOOSE_FILE_TITLE = "action3_chooseFile_title";
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
  
  public final String ACTION3_TEMPDIR_NAME = "action3_tempDir_name";
  public final String ACTION3_PROGRESS_DIALOG_TITLE = "action3_progresDialog_title";
  /**
   * Preview dialog title in apply package.
   */
  public final String PREVIEW = "preview";
  
  /**
   * en: Failed to apply package because of: 
   */
  public final String FAILED_TO_APPLY_PACKAGE = "failed_to_apply_package";
  
  public final String SHOW_REPORT_LABEL = "showReport_label";
  public final String SHOW_REPORT_TITLE = "showReport_dialog_title";
  public final String SHOW_REPORT_EXCEPTION_MESSAGE = "showReport_exception_message";
  
  public final String APPLY_BUTTON = "apply_button";
  
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
  
  
  public final String PREVIEW_DIALOG_PROGRESS_INFOMESSAGE = "previewDialog_progressDialog_infoMessage";
  public final String PREVIEW_DIALOG_PROGRESS_ERRORMESSAGE = "previewDialog_progressDialog_errorMessage";
  public final String PREVIEW_DIALOG_SUPPORTED_OXYFILE = "previewDialog_supportedOxyFile";
  public final String PREVIEW_DIALOG_CHECKBOX = "preview_dialog_checkBox";
  
  public final String PROGRESS_DIALOG_LABEL = "progressDialog_label";
  
  public final String CHANGE_MILESTONE_PROGRESS_TEXT = "changeMilestone_progressText";
  
  public final String COPYDIR_PROGRESS_TEXT = "copyDirectory_progressText";
  
  /**
   * en: A report will be created at: a_date.
   */
  public final String XHTML_REPORT_LOCATION = "report_will_be_created";
  
  public final String REPORT_DIALOG_CHECKBOX_TOOLTIP = "reportDialog_checkbox_tooltip";
  
  /**
   * en: Last report was created on: a_date.
   */
  public final String LAST_REPORT_CREATION_TIME = "last_report_creation_time";
  
  /**
   * Save
   */
  public final String SAVE = "save";
}
