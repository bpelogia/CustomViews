[![](https://jitpack.io/v/bpelogia/CustomViews.svg)](https://jitpack.io/#bpelogia/CustomViews)

# CustomViews
  ## Description
  Provide a package that contains EditText with masks, horizontal NumberPicker and some extensions utilities to View, EditText, String and Double.
  
  ## ScreenShot
  <p align="center"> <img width=40% height=40% src="https://github.com/bpelogia/CustomViews/blob/master/screenshot/Screenshot_customviews.png"></p>

## Features
  #### CustomMaskEditText with validation:
  - cpf -> ###.###.###-##;
  - plates -> AAA-####; 
  - monetary value -> R$#.###.###,##;
  - postal code -> #####-###;
  - date -> ##/##/####;
  - phone ->  (##) ####-#### / cellphone -> (##) #####-####
  - simple field -> without mask (validation of required field)
  #### CustomNumberPicker
  - horizontal 
  - vertical
  #### RxView
  - fromSearchView - return Observable\<String\>
  
## Extensions
  #### Double
  - formatMoney
  #### String
  - formatCEP
  - formatPhone
  - formatCPF
  - isCPFValid
  - isDateValid
  - unmaskMonetary
  - onlyNumbers
  - onlyLetters
  - onlyAlphanumerics
  #### View
  - expandMore
  - collapseMore
  - expand
  - collapse
  - moveViewDown
  - moveViewRight
  - moveViewLeft
  - moveViewTop
  #### NestedScrollView
  - moveDownViewOnScrolling
  #### EditText
  - isEmptyFieldOrZero
  - isZeroField
  
## Usage  
  ###  CustomNumberPicker
  #### XML

add `xmlns:app="http://schemas.android.com/apk/res-auto"`

```xml
<br.com.bpelogia.viewcustom.ui.CustomNumberPicker
    android:id="@+id/picker"
    android:layout_width="@dimen/numberpicker_year_width"
    android:layout_height="@dimen/numberpicker_year_height"
    android:layout_marginEnd="@dimen/common_big_spacing"
    android:layout_marginStart="@dimen/common_big_spacing"
    android:padding="0dp"
    app:np_dividerColor="@color/gray_border"
    app:np_dividerDistance="@dimen/numberpicker_year_diver_distance"
    app:np_max="2019"
    app:np_min="2009"
    app:np_value="2018"
    app:np_orientation="horizontal"
    app:np_textColor="@color/gray_text"
    app:np_textSize="@dimen/font_normal_size"
    app:np_wrapSelectorWheel="false" />

```

### Attributes

|attribute name|attribute description|
|:-:|:-:|
|np_width|The width of this widget.|
|np_height|The height of this widget.|
|np_dividerColor|The color of the selection divider.|
|np_dividerDistance|The distance between the two selection dividers.|
|np_dividerThickness|The thickness of the selection divider.|
|np_formatter|The formatter of the numbers.|
|np_max|The max value of this widget.|
|np_min|The min value of this widget.|
|np_orientation|The orientation of this widget. Default is vertical.|
|np_selectedTextColor|The text color of the selected number.|
|np_selectedTextSize|The text size of the selected number.|
|np_textColor|The text color of the numbers.|
|np_textSize|The text size of the numbers.|
|np_typeface|The typeface of the numbers.|
|np_value|The current value of this widget.|
|np_wheelItemCount|The number of items show in the selector wheel.|
|np_wrapSelectorWheel|Flag whether the selector should wrap around.|

###  CustomMaskEditText
  #### XML

  - add `xmlns:app="http://schemas.android.com/apk/res-auto"`

```xml
<!--Monetary-->
<android.support.design.widget.TextInputLayout
    android:id="@+id/ti_monetary"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginEnd="@dimen/common_big_spacing"
    android:layout_marginStart="@dimen/common_big_spacing"
    android:layout_weight="@integer/common_text_weight">

    <br.com.bpelogia.viewcustom.ui.CustomMaskEditText
        android:id="@+id/et_monetary"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="monetary"
        android:maxLines="1"
        app:mask="@string/monetary_mask_format"
        app:required="true" />

</android.support.design.widget.TextInputLayout>
```
  - change `app:mask` by `monetary_mask_format`, `cpf_mask_format`, `plate_mask_format`, `cep_mask_format`, `date_mask_format` or `phone_mask_format`

 #### Kotlin
  
  - Check if CustomMaskEditText is Valid like
 
 ```kotlin
val isValidField = et_monetary.isValid

```

## Gradle

Add the dependency in your `build.gradle`

```gradle
//Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
//Step 2. Add the dependency

	dependencies {
	        compile 'com.github.bpelogia:CustomViews:1.0.5'
	}
``` 
#### latestVersion: [![](https://jitpack.io/v/bpelogia/CustomViews.svg)](https://jitpack.io/#bpelogia/CustomViews)

  
  # Credits
I used [NumberPicker](https://github.com/ShawnLin013/NumberPicker) library by ShawnLin013(Shawn Lin) as a base for development.

  # License
```
Copyright 2018 Bruno Pelogia

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
