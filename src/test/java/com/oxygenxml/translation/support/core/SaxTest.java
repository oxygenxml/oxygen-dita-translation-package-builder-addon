package com.oxygenxml.translation.support.core;

import java.io.File;

import com.oxygenxml.translation.support.util.PathOption;

/**
 * TODO Write some tests for the map structure iteration. 
 *
 */
public class SaxTest {

  private PathOption pathOption = new PathOption();
  private File rootDir = pathOption.getPath("SAX-Test");
//
//  @Test
//  public void SaxParserTest() throws ParserConfigurationException, SAXException, IOException {
//
//    ArrayList<File> actual = new HrefFinder().gatherHrefAttributes(new CustomParserCreator(), rootDir.getPath() + "\\garage.ditamap");
//    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
//    for (File string : actual) {
//      actualResult.add(new ResourceInfo(string.getAbsolutePath()));
//    }
//
//    String expectedResult = "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\garageconceptsoverview.dita  null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\lawnmower.dita               null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\oil.dita                     null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\paint.dita                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\shelving.dita                null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\snowshovel.dita              null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\toolbox.dita                 null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\tools.dita                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\waterhose.dita               null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\wheelbarrow.dita             null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\workbench.dita               null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\concepts\\wwfluid.dita                 null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\carMaintenance.dita             null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\changingtheoil.dita             null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\garagetaskoverview.dita         null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\organizing.dita                 null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\shovellingsnow.dita             null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\spraypainting.dita              null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\takinggarbage.dita              null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\tasks\\washingthecar.dita              null\n" +
//        "";
//    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
//    System.out.println(DumpUtil.dump(actualResult));
//
//  }
//
//  @Test
//  public void SaxParserTest_2() throws ParserConfigurationException, SAXException, IOException {
//
//    ArrayList<File> actual = new HrefFinder().gatherHrefAttributes(new CustomParserCreator(), rootDir.getPath() + "\\UserManual.ditamap");
//    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
//    for (File string : actual) {
//      actualResult.add(new ResourceInfo(string.getAbsolutePath()));
//    }
//
//    String expectedResult = "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-author-dita.ditamap                    null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-authoring-customization-guide.ditamap  null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-common-problems.ditamap                null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-configure-application.ditamap          null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-content-fusion.ditamap                 null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-debugging.ditamap                      null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-editing-documents.ditamap              null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-extending-eclipse.ditamap.ditamap      null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-extending-oxygen.ditamap               null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-getting-started.ditamap                null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-import.ditamap                         null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-installation.ditamap                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-introduction.ditamap                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-oxygen-editing-modes.ditamap           null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-oxygen-tools.ditamap                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-perspectives.ditamap                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-predefined-document-types.ditamap      null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-querying-documents.ditamap             null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-svn-client.ditamap                     null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-transforming-documents.ditamap         null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-using-the-webapp-reviewer.ditamap      null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-working-with-archives.ditamap          null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\chapter-working-with-databases.ditamap         null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\externalReferences.ditamap                     null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\glossary.ditamap                               null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\keydefs.ditamap                                null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\maps\\productSbjSchemeVals.ditamap                   null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\reusables\\reusables.ditamap                         null\n" +
//        "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\topics\\copyright.dita                               null\n" + 
//        "";
//    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
//    System.out.println(DumpUtil.dump(actualResult));
//
//  }
//  
//  @Test
//  public void findHrefsTest() throws ParserConfigurationException, SAXException, IOException {
//    File ditaMap = new File(rootDir.getPath() + "\\it-book\\taskbook.ditamap");
//    
//    ArrayList<File> actual = new SaxResourceIteration().listResources(new CustomParserCreator(), ditaMap);
//    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
//    for (File string : actual) {
//      actualResult.add(new ResourceInfo(string.getAbsolutePath()));
//    }
//    
////    for (ResourceInfo resourceInfo : actualResult) {
////      System.out.println(resourceInfo.toString());
////    }
//    String expectedResult = "D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\concepts\\notices.dita            null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\concepts\\taskbook-abstract.dita  null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\taskbook.ditamap                 null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\closeprograms.dita         null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\configuredatabase.dita     null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\configurestorage.dita      null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\configurewebserver.dita    null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\configuring.dita           null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\databasetrouble.dita       null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\drivetrouble.dita          null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\insertdrive.dita           null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\installdb.dita             null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\installstorage.dita        null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\installwebserver.dita      null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\maintaindatabase.dita      null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\maintaining.dita           null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\maintainserver.dita        null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\maintainstorage.dita       null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\replacecover.dita          null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\restart.dita               null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\runsetup.dita              null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\troubleshooting.dita       null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\unscrewcover.dita          null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\tasks\\webtrouble.dita            null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\topics\\installing.dita           null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\topics\\task_appendix.dita        null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\topics\\task_preface.dita         null\n" +
//"D:\\pluginWorkspace\\Translation-Package-Builder\\src\\test\\resources\\SAX-Test\\it-book\\topics\\trademarks.dita           null\n" + 
//"";
//    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
//    System.out.println(DumpUtil.dump(actualResult));
//    
//  }

}
