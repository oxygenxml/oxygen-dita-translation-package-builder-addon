package com.oxygenxml.translation.support.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.oxygenxml.translation.support.core.models.ResourceInfo;
import com.oxygenxml.translation.support.util.CustomParserCreator;
import com.oxygenxml.translation.support.util.PathOption;


public class SaxTest {

  private PathOption pathOption = new PathOption();
  private File rootDir = pathOption.getPath("SAX-Test");

  @Test
  public void SaxParserTest() throws ParserConfigurationException, SAXException, IOException {

    ArrayList<File> actual = new HrefFinder().gatherHrefAttributes(new CustomParserCreator(), rootDir.getPath() + "\\garage.ditamap");
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    for (File string : actual) {
      actualResult.add(new ResourceInfo(string.getAbsolutePath()));
    }

    String expectedResult = "concepts/garageconceptsoverview.dita  null\n" +
        "concepts/lawnmower.dita               null\n" +
        "concepts/oil.dita                     null\n" +
        "concepts/paint.dita                   null\n" +
        "concepts/shelving.dita                null\n" +
        "concepts/snowshovel.dita              null\n" +
        "concepts/toolbox.dita                 null\n" +
        "concepts/tools.dita                   null\n" +
        "concepts/waterhose.dita               null\n" +
        "concepts/wheelbarrow.dita             null\n" +
        "concepts/workbench.dita               null\n" +
        "concepts/wwfluid.dita                 null\n" +
        "tasks/carMaintenance.dita             null\n" +
        "tasks/changingtheoil.dita             null\n" +
        "tasks/garagetaskoverview.dita         null\n" +
        "tasks/organizing.dita                 null\n" +
        "tasks/shovellingsnow.dita             null\n" +
        "tasks/spraypainting.dita              null\n" +
        "tasks/takinggarbage.dita              null\n" +
        "tasks/washingthecar.dita              null\n" +
        "";
    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
    System.out.println(DumpUtil.dump(actualResult));

  }

  @Test
  public void SaxParserTest_2() throws ParserConfigurationException, SAXException, IOException {

    ArrayList<File> actual = new HrefFinder().gatherHrefAttributes(new CustomParserCreator(), rootDir.getPath() + "\\UserManual.ditamap");
    ArrayList<ResourceInfo> actualResult = new ArrayList<ResourceInfo>();
    for (File string : actual) {
      actualResult.add(new ResourceInfo(string.getAbsolutePath()));
    }

    String expectedResult = "maps/chapter-author-dita.ditamap                    null\n" +
        "maps/chapter-authoring-customization-guide.ditamap  null\n" +
        "maps/chapter-common-problems.ditamap                null\n" +
        "maps/chapter-configure-application.ditamap          null\n" +
        "maps/chapter-content-fusion.ditamap                 null\n" +
        "maps/chapter-debugging.ditamap                      null\n" +
        "maps/chapter-editing-documents.ditamap              null\n" +
        "maps/chapter-extending-eclipse.ditamap.ditamap      null\n" +
        "maps/chapter-extending-oxygen.ditamap               null\n" +
        "maps/chapter-getting-started.ditamap                null\n" +
        "maps/chapter-import.ditamap                         null\n" +
        "maps/chapter-installation.ditamap                   null\n" +
        "maps/chapter-introduction.ditamap                   null\n" +
        "maps/chapter-oxygen-editing-modes.ditamap           null\n" +
        "maps/chapter-oxygen-tools.ditamap                   null\n" +
        "maps/chapter-perspectives.ditamap                   null\n" +
        "maps/chapter-predefined-document-types.ditamap      null\n" +
        "maps/chapter-querying-documents.ditamap             null\n" +
        "maps/chapter-svn-client.ditamap                     null\n" +
        "maps/chapter-transforming-documents.ditamap         null\n" +
        "maps/chapter-using-the-webapp-reviewer.ditamap      null\n" +
        "maps/chapter-working-with-archives.ditamap          null\n" +
        "maps/chapter-working-with-databases.ditamap         null\n" +
        "maps/externalReferences.ditamap                     null\n" +
        "maps/glossary.ditamap                               null\n" +
        "maps/keydefs.ditamap                                null\n" +
        "maps/productSbjSchemeVals.ditamap                   null\n" +
        "reusables/reusables.ditamap                         null\n" +
        "topics/copyright.dita                               null\n" + 
        "";
    Assert.assertEquals(expectedResult, DumpUtil.dump(actualResult));
    System.out.println(DumpUtil.dump(actualResult));

  }

}
