package com.oxygenxml.translation.support.util;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

public class FontProperties {
  
  /**
   * Private constructor for utility class.
   */
  private FontProperties() {
    // There's no hope!
  }
  
  /**
   * A map holding the necessary text attributes to render underlined text.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final Map<? extends Attribute, ?> UNDERLINED_TEXT_ATTRIBUTES_MAP = new HashMap() { /*NOSONAR*/
    {/*NOSONAR*/
      // Enable the underline font attribute
      put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
    }
  };
}
