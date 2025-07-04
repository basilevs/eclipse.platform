/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ShowWhitespaceAction extends TextEditorPropertyAction {

	private Map<MergeSourceViewer, WhitespaceCharacterPainter> fPainters;
	private boolean isWhitespaceShowing;
	private final boolean[] fNeedsPainters;
	/** @since 3.7 */
	private boolean fShowLeadingSpaces;
	/** @since 3.7 */
	private boolean fShowEnclosedSpaces;
	/** @since 3.7 */
	private boolean fShowTrailingSpaces;
	/** @since 3.7 */
	private boolean fShowLeadingIdeographicSpaces;
	/** @since 3.7 */
	private boolean fShowEnclosedIdeographicSpaces;
	/** @since 3.7 */
	private boolean fShowTrailingIdeographicSpace;
	/** @since 3.7 */
	private boolean fShowLeadingTabs;
	/** @since 3.7 */
	private boolean fShowEnclosedTabs;
	/** @since 3.7 */
	private boolean fShowTrailingTabs;
	/** @since 3.7 */
	private boolean fShowCarriageReturn;
	/** @since 3.7 */
	private boolean fShowLineFeed;
	/** @since 3.7 */
	private final IPreferenceStore fStore = EditorsUI.getPreferenceStore();
	/** @since 3.7 */
	private int fAlpha;

	public ShowWhitespaceAction(MergeSourceViewer[] viewers, boolean[] needsPainters) {
		super(CompareMessages.ShowWhitespaceAction_0, viewers, AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS);
		fNeedsPainters = needsPainters;
		synchronizeWithPreference();
	}

	@Override
	protected void synchronizeWithPreference() {
		boolean checked = false;
		if (fStore != null) {
			checked = fStore.getBoolean(getPreferenceKey());
			fShowLeadingSpaces = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_LEADING_SPACES);
			fShowEnclosedSpaces = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_SPACES);
			fShowTrailingSpaces = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_TRAILING_SPACES);
			fShowLeadingIdeographicSpaces = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES);
			fShowEnclosedIdeographicSpaces = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES);
			fShowTrailingIdeographicSpace = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES);
			fShowLeadingTabs = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_LEADING_TABS);
			fShowEnclosedTabs = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_TABS);
			fShowTrailingTabs = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_TRAILING_TABS);
			fShowCarriageReturn = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_CARRIAGE_RETURN);
			fShowLineFeed = fStore.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_LINE_FEED);
			fAlpha = fStore.getInt(AbstractTextEditor.PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE);
		}
		if (checked != isChecked()) {
			if (toggleState(checked)) {
				setChecked(checked);
			}
		} else if (fNeedsPainters != null && checked) {
			hideWhitespace();
			showWhitespace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(getPreferenceKey()) || AbstractTextEditor.PREFERENCE_SHOW_LEADING_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_TRAILING_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_LEADING_TABS.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_TABS.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_TRAILING_TABS.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_CARRIAGE_RETURN.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_LINE_FEED.equals(property) || AbstractTextEditor.PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE.equals(property)) {
			synchronizeWithPreference();
		}
	}

	@Override
	protected boolean toggleState(boolean checked) {
		if (fNeedsPainters == null) {
			return false; // Not initialized yet
		}
		if (checked) {
			showWhitespace();
		} else {
			hideWhitespace();
		}
		return true;
	}

	private synchronized Map<MergeSourceViewer, WhitespaceCharacterPainter> getPainters() {
		if (fPainters == null) {
			fPainters = new HashMap<>();
		}
		return fPainters;
	}

	private void showWhitespace() {
		if (isWhitespaceShowing) {
			return;
		}
		try {
			Map<MergeSourceViewer, WhitespaceCharacterPainter> painters = getPainters();
			MergeSourceViewer[] viewers = getViewers();
			for (int i = 0; i < viewers.length; i++) {
				if (fNeedsPainters[i]) {
					MergeSourceViewer viewer = viewers[i];
					if (viewer == null) {
						continue;
					}
					SourceViewer sourceViewer = viewer.getSourceViewer();
					WhitespaceCharacterPainter painter;
					if (fStore != null) {
						painter = new WhitespaceCharacterPainter(sourceViewer, fShowLeadingSpaces, fShowEnclosedSpaces, fShowTrailingSpaces, fShowLeadingIdeographicSpaces,
								fShowEnclosedIdeographicSpaces, fShowTrailingIdeographicSpace, fShowLeadingTabs, fShowEnclosedTabs, fShowTrailingTabs, fShowCarriageReturn, fShowLineFeed, fAlpha);
					} else {
						painter = new WhitespaceCharacterPainter(sourceViewer);
					}
					sourceViewer.addPainter(painter);
					painters.put(viewer, painter);
				}
			}
		} finally {
			isWhitespaceShowing = true;
		}
	}

	private void hideWhitespace() {
		Map<MergeSourceViewer, WhitespaceCharacterPainter> painters = getPainters();
		for (MergeSourceViewer viewer : painters.keySet()) {
			WhitespaceCharacterPainter painter = painters.get(viewer);
			if (painter != null) {
				viewer.getSourceViewer().removePainter(painter);
				painter.deactivate(true);
			}
		}
		painters.clear();
		isWhitespaceShowing = false;
	}

}
