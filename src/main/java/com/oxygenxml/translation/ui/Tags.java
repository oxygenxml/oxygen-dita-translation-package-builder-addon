package com.oxygenxml.translation.ui;
/**
 * An interface used for accessing the key values from the "i18n/translation.xml" file.
 */
public interface Tags {
  
  public final String TRANSLATION_PACKAGE_BUILDER_PLUIGIN_NAME = "translation_package_builder_plugin_name";
  public final String JMENU_ITEM1 = "jmenu_item1";
  public final String JMENU_TOOLTIP_ITEM1 = "jmenu_tooltip_item1";
  
  /**
   * Name of the option that generates the archive with modified files.
   * 
   * en: Create modified files package
   */
  public final String CREATE_MODIFIED_FILES_PACKAGE = "create_modified_files_package";
  
  public final String JMENU_TOOLTIP_ITEM2 = "jmenu_tooltip_item2";
  public final String JMENU_ITEM3 = "jmenu_item3";
  public final String JMENU_TOOLTIP_ITEM3 ="jmenu_tooltip_item3";
  
  public final String ACTION1_INFO_MESSAGE = "action1_infoMessage";
  
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
  
  public final String ACTION2_PROGRESS_DIALOG_TITLE = "action2_progresDialog_title";
  
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
  
  public final String PACKAGEBUILDER_PROGRESS_TEXT1 = "packageBuilder_progressText1";
  public final String PACKAGEBUILDER_PROGRESS_TEXT2 = "packageBuilder_progressText2";
  public final String CHANGE_MILESTONE_PROGRESS_TEXT = "changeMilestone_progressText";
  
  public final String ZIPDIR_PROGRESS_TEXT = "zipDirectory_progressText";
  public final String UNZIPDIR_PROGRESS_TEXT = "unzipDirectory_progressText";
  public final String COPYDIR_PROGRESS_TEXT = "copyDirectory_progressText";
  public final String GENERATE_MODIFIED_FILES_PROGRESS_MESSAGE1 = "generateModified_progress_message1";
  public final String GENERATE_MODIFIED_FILES_PROGRESS_MESSAGE2 = "generateModified_progress_message2";
  
  /**
   * en: A report will be created at: a_date.
   */
  public final String XHTML_REPORT_LOCATION = "report_will_be_created";
  
  public final String REPORT_DIALOG_CHECKBOX_TOOLTIP = "reportDialog_checkbox_tooltip";
  
  /**
   * en: Last report was created on: a_date.
   */
  public final String LAST_REPORT_CREATION_TIME = "last_report_creation_time";
  
  public final String REPORT_DIALOG_LOGGER_MESSAGE = "reportDialog_logger_message1";
  public final String REPORT_DIALOG_FILE_DESCRIPTOR = "reportDialog_fileDescriptor";
  public final String REPORT_DIALOG_CHECKBOX_TEXT = "reportDialog_checkbox_text";
  
  /**
   * Save
   */
  public final String SAVE = "save";
}
