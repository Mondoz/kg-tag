package com.hiekn.kg.service.tagging.service;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

public class AnsjTest {
	public static void main(String[] args) {
		DicLibrary.insert(DicLibrary.DEFAULT, "年洁");
		DicLibrary.insert(DicLibrary.DEFAULT, "有多");
		DicLibrary.insert(DicLibrary.DEFAULT, "aron swartz");
		DicLibrary.insert(DicLibrary.DEFAULT, "摩拜单车");
		String str = "\\\"abstract\\\": { \\\"original_lang\\\": \\\"zh-cn\\\", \\\"original\\\": \\\"本发明公开了一种显示装置及封装方法，所述显示装置包括：显示面板、盖板和缓冲层；其中，所述盖板位于所述显示面板的出光侧，所述缓冲层位于所述盖板与所述显示面板之间，且所述缓冲层位于所述显示面板的非显示区。所述显示装置的封装方法，包括：在显示面板的非显示区形成缓冲层；在所述显示面板的显示区形成液态光学胶；在所述缓冲层以及所述液态光学胶上方盖合盖板后，进行紫外光固化，实现了增强产品机械抗冲击能力和跌落震动能力的目的。\\\", \\\"en\\\": \\\"The invention discloses a display device and an encapsulating method of the display device. The display device comprises a display panel, a cover board and a buffer layer, wherein the cover board is positioned at the light emitting side of the display panel, the buffer layer is located between the cover board and the display panel, and the buffer layer is located on a non-display region of the display panel. The encapsulating method of the display device comprises the steps of: forming a buffer layer on the non-display region of the display panel; forming liquid optical cement on a display region of the display panel; covering the cover board above the buffer layer and the liquid optical cement and then carrying out ultraviolet light curing, thereby achieving the purposes of enhancing the mechanical shock resistance and the falling vibration ability of the products.\\\" }," ;
		Result parse = ToAnalysis.parse(str);
		System.out.println(parse);
	}
}
