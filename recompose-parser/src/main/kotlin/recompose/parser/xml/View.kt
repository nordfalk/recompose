/*
 * Copyright 2020 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package recompose.parser.xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.START_TAG
import recompose.ast.Node
import recompose.ast.attributes.ViewAttributes
import recompose.ast.values.LayoutSize
import recompose.parser.Parser
import recompose.parser.values.constraints
import recompose.parser.values.drawable
import recompose.parser.values.layoutSize
import recompose.parser.values.padding
import recompose.parser.xml.view.button
import recompose.parser.xml.view.cardView
import recompose.parser.xml.view.checkBox
import recompose.parser.xml.view.editText
import recompose.parser.xml.view.imageView
import recompose.parser.xml.view.radioButton
import recompose.parser.xml.view.rangeSlider
import recompose.parser.xml.view.switch
import recompose.parser.xml.view.textView
import recompose.parser.xml.view.view
import recompose.parser.xml.viewgroup.constraintLayout
import recompose.parser.xml.viewgroup.frameLayout
import recompose.parser.xml.viewgroup.linearLayout
import recompose.parser.xml.viewgroup.unknown

/**
 * Parses an element representing a `View` implementation.
 */
internal fun XmlPullParser.node(): Node {
    require(START_TAG, null, null)

    return when (name) {
        // ViewGroupNode
        "LinearLayout" -> linearLayout()
        "FrameLayout" -> frameLayout()
        // AndroidX
        "androidx.constraintlayout.widget.ConstraintLayout" -> constraintLayout()
        "androidx.cardview.widget.CardView",
        "com.google.android.material.card.MaterialCardView" -> cardView()

        else -> {
            val viewNode = when (name) {
                // ViewNode
                "View" -> view()
                "TextView" -> textView()
                "androidx.appcompat.widget.AppCompatImageView",
                "ImageView" -> imageView()
                "Button" -> button()
                "CheckBox" -> checkBox()
                "RadioButton" -> radioButton()
                "EditText" -> editText()
                "Switch" -> switch()

                // AndroidX
                "com.google.android.material.slider.RangeSlider" -> rangeSlider()

                else -> {
                    println("Unknown view: $name")
                    unknown()
                }
            }

            skipToEndTag()
            viewNode
        }
    }
}
/**
 * Skips to the end of the tag
 */
internal fun XmlPullParser.skipToEndTag() {
    if (getEventType() == XmlPullParser.END_TAG) return
    @Suppress("ControlFlowWithEmptyBody")
    while (next() != XmlPullParser.END_TAG);
}

/**
 * * Parses the attributes that are shared between all `View` implementations.
 */
internal fun XmlPullParser.viewAttributes(): ViewAttributes {
    return ViewAttributes(
        id = id(),
        width = layoutSize("android:layout_width") ?: LayoutSize.WrapContent,
        height = layoutSize("android:layout_height") ?: LayoutSize.WrapContent,
        background = drawable("android:background"),
        padding = padding(),
        constraints = constraints()
    )
}

@Suppress("MagicNumber")
internal fun XmlPullParser.id(name: String = "android:id"): String? {
    val id = getAttributeValue(null, name) ?: return null

    return when {
        id.startsWith("@+id/") -> id.substring(5)
        id.startsWith("@id/") -> id.substring(4)
        else -> throw Parser.ParserException("Cannot parse id: $id")
    }
}
