/**
 */
package org.eclipse.fx.code.editor.ldef.lDef;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Scanner Pattern Rule</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fx.code.editor.ldef.lDef.Scanner_PatternRule#getStartPattern <em>Start Pattern</em>}</li>
 *   <li>{@link org.eclipse.fx.code.editor.ldef.lDef.Scanner_PatternRule#getContentPattern <em>Content Pattern</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fx.code.editor.ldef.lDef.LDefPackage#getScanner_PatternRule()
 * @model
 * @generated
 */
public interface Scanner_PatternRule extends Scanner_Rule
{
  /**
   * Returns the value of the '<em><b>Start Pattern</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Start Pattern</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Start Pattern</em>' attribute.
   * @see #setStartPattern(String)
   * @see org.eclipse.fx.code.editor.ldef.lDef.LDefPackage#getScanner_PatternRule_StartPattern()
   * @model
   * @generated
   */
  String getStartPattern();

  /**
   * Sets the value of the '{@link org.eclipse.fx.code.editor.ldef.lDef.Scanner_PatternRule#getStartPattern <em>Start Pattern</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Start Pattern</em>' attribute.
   * @see #getStartPattern()
   * @generated
   */
  void setStartPattern(String value);

  /**
   * Returns the value of the '<em><b>Content Pattern</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Content Pattern</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Content Pattern</em>' attribute.
   * @see #setContentPattern(String)
   * @see org.eclipse.fx.code.editor.ldef.lDef.LDefPackage#getScanner_PatternRule_ContentPattern()
   * @model
   * @generated
   */
  String getContentPattern();

  /**
   * Sets the value of the '{@link org.eclipse.fx.code.editor.ldef.lDef.Scanner_PatternRule#getContentPattern <em>Content Pattern</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Content Pattern</em>' attribute.
   * @see #getContentPattern()
   * @generated
   */
  void setContentPattern(String value);

} // Scanner_PatternRule
