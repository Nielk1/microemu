/*
 * MicroEmulator 
 * Copyright (C) 2001 Bartek Teodorczyk <barteo@barteo.net>
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.microemu.device.swt;

import java.util.Enumeration;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.microemu.CommandManager;
import org.microemu.MIDletBridge;
import org.microemu.device.DeviceFactory;
import org.microemu.device.InputMethodEvent;
import org.microemu.device.impl.InputMethodImpl;
import org.microemu.device.impl.SoftButton;


public class SwtInputMethod extends InputMethodImpl 
{
	public int getGameAction(int keyCode)
    {
        // TODO poprawic KeyEvent
        switch (keyCode) {
            case SWT.ARROW_UP:
                return Canvas.UP;

            case SWT.ARROW_DOWN:
                return Canvas.DOWN;

            case SWT.ARROW_LEFT:
                return Canvas.LEFT;

            case SWT.ARROW_RIGHT:
                return Canvas.RIGHT;

            case SWT.CR:
                return Canvas.FIRE;

            /*
             * case KeyEvent.VK_1: case KeyEvent.VK_A: return Canvas.GAME_A;
             * 
             * case KeyEvent.VK_3: case KeyEvent.VK_B: return Canvas.GAME_B;
             * 
             * case KeyEvent.VK_7: case KeyEvent.VK_C: return Canvas.GAME_C;
             * 
             * case KeyEvent.VK_9: case KeyEvent.VK_D: return Canvas.GAME_D;
             */

            default:
                return 0;
        }
    }

	
    public int getKeyCode(int gameAction)
    {
        // TODO poprawic KeyEvent
        switch (gameAction) {
            case Canvas.UP:
                return SWT.ARROW_UP;

            case Canvas.DOWN:
                return SWT.ARROW_DOWN;

            case Canvas.LEFT:
                return SWT.ARROW_LEFT;

            case Canvas.RIGHT:
                return SWT.ARROW_RIGHT;

            case Canvas.FIRE:
                return SWT.CR;

            /*
             * case Canvas.GAME_A: return KeyEvent.VK_1;
             * 
             * case Canvas.GAME_B: return KeyEvent.VK_3;
             * 
             * case Canvas.GAME_C: return KeyEvent.VK_7;
             * 
             * case Canvas.GAME_D: return KeyEvent.VK_9;
             */

            default:
                throw new IllegalArgumentException();
        }
    }


	public String getKeyName(int keyCode)
    {
	      if (keyCode == SWT.F1) {
	    	  return "SOFT1";
	      } else if (keyCode == SWT.F2) {
	    	  return "SOFT2";
	      } else if (keyCode == SWT.CR) {
	    	  return "SELECT";
	      }
	          
          return Integer.toString(keyCode);
    }

	
	private boolean commonKeyPressed(KeyEvent ev) 
	{
		String tmp;

		int keyCode = ev.keyCode;
		if (inputMethodListener == null) {
			int midpKeyCode;
			switch (ev.character) {
				case '*' :
					midpKeyCode = Canvas.KEY_STAR;
					break;
				case '#' :
					midpKeyCode = Canvas.KEY_POUND;
					break;
				default :
					midpKeyCode = keyCode;
			}
			MIDletBridge.getMIDletAccess().getDisplayAccess().keyPressed(midpKeyCode);
			return true;
		}

		if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN) {
			MIDletBridge.getMIDletAccess().getDisplayAccess().keyPressed(keyCode);
			return true;
		}

		// TODO poprawic KeyEvent
		/*
		 * if (keyCode == KeyEvent.VK_MODECHANGE) { if (getInputMode() ==
		 * InputMethod.INPUT_123) { setInputMode(InputMethod.INPUT_ABC_UPPER); }
		 * else if (getInputMode() == InputMethod.INPUT_ABC_UPPER) {
		 * setInputMode(InputMethod.INPUT_ABC_LOWER); } else if (getInputMode() ==
		 * InputMethod.INPUT_ABC_LOWER) { setInputMode(InputMethod.INPUT_123); }
		 * synchronized (this) { if (lastButton != null) { caret++; lastButton =
		 * null; lastButtonCharIndex = -1; } } InputMethodEvent event = new
		 * InputMethodEvent(InputMethodEvent.CARET_POSITION_CHANGED, caret,
		 * text); inputMethodListener.caretPositionChanged(event); return true;
		 */

		if (keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT) {
			synchronized (this) {
				if (keyCode == SWT.ARROW_LEFT && caret > 0) {
					caret--;
				}
				if (keyCode == SWT.ARROW_RIGHT && caret < text.length()) {
					caret++;
				}
				lastButton = null;
				lastButtonCharIndex = -1;
			}
			InputMethodEvent event = new InputMethodEvent(InputMethodEvent.CARET_POSITION_CHANGED, caret, text);
			inputMethodListener.caretPositionChanged(event);
			return true;
		}

		if (keyCode == SWT.BS) {
			synchronized (this) {
				if (lastButton != null) {
					caret++;
					lastButton = null;
					lastButtonCharIndex = -1;
				}
				if (caret > 0) {
					caret--;
					tmp = "";
					if (caret > 0) {
						tmp += text.substring(0, caret);
					}
					if (caret < text.length() - 1) {
						tmp += text.substring(caret + 1);
					}
					text = tmp;
				}
			}
			InputMethodEvent event = new InputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, caret, text);
			inputMethodListener.inputMethodTextChanged(event);
			event = new InputMethodEvent(InputMethodEvent.CARET_POSITION_CHANGED, caret, text);
			inputMethodListener.caretPositionChanged(event);
			return true;
		}
		
		if (keyCode == SWT.SHIFT || keyCode == SWT.CTRL || keyCode == SWT.ALT) {
			return true;
		}

		return false;
	}

	
	public void keyPressed(KeyEvent ev) 
	{
		// invoke any associated commands, but send the raw key codes instead
		boolean rawSoftKeys = DeviceFactory.getDevice().getDeviceDisplay().isFullScreenMode();
		SwtButton pressedButton = getButton(ev);
		if (pressedButton != null) {
		    if (pressedButton instanceof SoftButton && !rawSoftKeys) {
			    Command cmd = ((SoftButton) pressedButton).getCommand();
			    if (cmd != null) {
					CommandManager.getInstance().commandAction(cmd);
					return;
			    }
			}
		}

		if (commonKeyPressed(ev)) {
			return;
		}

		if (text.length() < maxSize && (ev.keyCode & SWT.EMBEDDED) == 0) {
			char[] test = new char[1];
			test[0] = ev.character;
			test = filterConstraints(test);
			if (test.length > 0) {
				synchronized (this) {
					if (lastButton != null) {
						caret++;
						lastButton = null;
						lastButtonCharIndex = -1;
					}
					String tmp = "";
					if (caret > 0) {
						tmp += text.substring(0, caret);
					}
					tmp += ev.character;
					if (caret < text.length()) {
						tmp += text.substring(caret);
					}
					text = tmp;
					caret++;
				}
				InputMethodEvent event = new InputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, caret, text);
				inputMethodListener.inputMethodTextChanged(event);
				event = new InputMethodEvent(InputMethodEvent.CARET_POSITION_CHANGED, caret, text);
				inputMethodListener.caretPositionChanged(event);
			}
		}
	}

	
	public void mousePressed(KeyEvent ev) 
	{
		String tmp;

		if (commonKeyPressed(ev)) {
			return;
		}

		if (text.length() < maxSize) {
			for (Enumeration e = DeviceFactory.getDevice().getButtons().elements(); e.hasMoreElements();) {
				SwtButton button = (SwtButton) e.nextElement();
				if (ev.keyCode == button.getKey()) {
					synchronized (this) {
						lastButtonCharIndex++;
						char[] buttonChars = filterConstraints(filterInputMode(button.getChars()));
						if (buttonChars.length > 0) {
							if (lastButtonCharIndex == buttonChars.length) {
								if (buttonChars.length == 1) {
									if (lastButton != null) {
										caret++;
									}
									lastButton = null;
								} else {
									lastButtonCharIndex = 0;
								}
							}
							if (lastButton != button) {
								if (lastButton != null) {
									caret++;
								}
								tmp = "";
								if (caret > 0) {
									tmp += text.substring(0, caret);
								}
								tmp += buttonChars[0];
								if (caret < text.length()) {
									tmp += text.substring(caret);
								}
								text = tmp;
								lastButton = button;
								lastButtonCharIndex = 0;
							} else {
								tmp = "";
								if (caret > 0) {
									tmp += text.substring(0, caret);
								}
								tmp += buttonChars[lastButtonCharIndex];
								if (caret < text.length() - 1) {
									tmp += text.substring(caret + 1);
								}
								text = tmp;
								lastButton = button;
							}
						} else {
							lastButton = null;
							lastButtonCharIndex = -1;
						}
						resetKey = false;
						notify();
					}

					InputMethodEvent event = new InputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, caret, text);
					inputMethodListener.inputMethodTextChanged(event);
					break;
				}
			}
		}
	}

	
	public void keyReleased(KeyEvent ev) 
	{
		MIDletBridge.getMIDletAccess().getDisplayAccess().keyReleased(ev.keyCode);
	}

	
	public void mouseReleased(int keyCode) 
	{
		MIDletBridge.getMIDletAccess().getDisplayAccess().keyReleased(keyCode);
	}

	
	public SwtButton getButton(KeyEvent ev)
	{
		for (Enumeration e = DeviceFactory.getDevice().getButtons().elements(); e.hasMoreElements(); ) {
			SwtButton button = (SwtButton) e.nextElement();
			if (ev.keyCode == button.getKey()) {
				return button;
			}
			if (button.isChar(ev.character)) {
				return button;
			}
		}
		        
		return null;
	}

}