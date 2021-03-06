package org.eclipse.fx.ide.fxml.compiler

import com.google.inject.Inject
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Stack
import org.eclipse.fx.ide.fxgraph.converter.IFXMLFile
import org.eclipse.fx.ide.fxgraph.fXGraph.Element
import org.eclipse.fx.ide.fxgraph.fXGraph.FXGraphFactory
import org.eclipse.fx.ide.fxgraph.fXGraph.ListValueProperty
import org.eclipse.fx.ide.fxgraph.fXGraph.Model
import org.eclipse.fx.ide.fxgraph.fXGraph.Property
import org.eclipse.fx.ide.fxgraph.fXGraph.StaticCallValueProperty
import org.eclipse.xtext.common.types.TypesFactory
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.eclipse.fx.ide.fxgraph.fXGraph.util.FXGraphAdapterFactory

class FXMLSaxHandler extends DefaultHandler {
	public Model model;

	Stack<Object> stack = new Stack;

	private final IJvmTypeProvider provider;

	private final Map<String,String> simpleToFqn
	private final List<String> globalImports
	private IFXMLFile file;
	private Map<String,Element> idMap = new HashMap
	private boolean valueOfType = false;

	private static String FXML_NAMESPACE = "http://javafx.com/fxml"
	private static String FXML_NAMESPACE_1 = "http://javafx.com/fxml/1"

	@Inject
	new(IJvmTypeProvider.Factory jdtTypeProviderFactory)
	{
		this.provider = jdtTypeProviderFactory.createTypeProvider
		this.globalImports = new ArrayList
		this.simpleToFqn = new HashMap
		this.globalImports.add("java.lang");
	}

	def setFile(IFXMLFile file) {
		this.file = file;
	}

	override startDocument() throws SAXException {
		model = FXGraphFactory.eINSTANCE.createModel
		val pack = FXGraphFactory.eINSTANCE.createPackageDeclaration
		pack.name = file?.packagename
		model.package = pack

		val componentDef = FXGraphFactory.eINSTANCE.createComponentDefinition
		componentDef.name = file?.name.substring(0,file?.name.indexOf('.'))
		model.componentDef = componentDef
	}

	override processingInstruction(String target, String data) throws SAXException {
		if ("import" == target) {
			val i = FXGraphFactory.eINSTANCE.createImport
			i.importedNamespace = data
			model.imports += i
			if( ! i.importedNamespace.endsWith("*") ) {
				simpleToFqn.put(
					i.importedNamespace.substring(i.importedNamespace.lastIndexOf('.')+1),
					i.importedNamespace
				)
			} else {
				globalImports.add(i.importedNamespace.substring(0,i.importedNamespace.lastIndexOf('.')))
			}
		}
	}

	def String getFXAttributeValue(Attributes attributes, String name) {
		var v = attributes.getValue(FXML_NAMESPACE,name);

		if( v == null ) {
			v = attributes.getValue(FXML_NAMESPACE_1,name);
		}

		return v;
	}

	def boolean isFXNamedSpace(String uri) {
		return uri == FXML_NAMESPACE || uri == FXML_NAMESPACE_1;
	}

	override startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if( ! uri.isFXNamedSpace && localName.contains('.') ) {
			// A static property
			val e = stack.peek as Element
			val prop = FXGraphFactory.eINSTANCE.createStaticCallValueProperty
			prop.name = localName.substring(localName.indexOf('.')+1)

			val t = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference()
			t.type = provider.findTypeByName(ReflectionHelper.getFqnType(localName.substring(0,localName.indexOf('.')),simpleToFqn,globalImports))

			prop.type = t

			e.staticCallProperties.add(prop);
			stack.push(prop)
		} else if( ! uri.isFXNamedSpace && Character.isLowerCase(localName.charAt(0)) ) {
			// A property
			val e = stack.peek as Element
			val prop = FXGraphFactory.eINSTANCE.createProperty
			prop.name = localName

			val vt = ReflectionHelper.getValueType(e.type.type,localName)

			if( vt == ValueType.LIST ) {
				val lp = FXGraphFactory.eINSTANCE.createListValueProperty
				prop.setValue(lp)
			}

			e.properties += prop

			stack.push(prop)
		} else {
			if( attributes.getFXAttributeValue("value") != null ) {
				val p = stack.peek as Property;
				val lp = p.value as ListValueProperty
				val vp = FXGraphFactory.eINSTANCE.createSimpleValueProperty
				vp.setStringValue(attributes.getFXAttributeValue("value"));
				lp.value += vp;
				valueOfType = true;
				return;
			}

			// An element
			val e = FXGraphFactory.eINSTANCE.createElement

			val isDynRoot = localName == 'root' && uri.isFXNamedSpace
			val t = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference();
			if( isDynRoot ) {
				model.componentDef.dynamicRoot = true;
				t.type = provider.findTypeByName(ReflectionHelper.getFqnType(attributes.getValue("type"),simpleToFqn,globalImports))
			} else {
				t.type = provider.findTypeByName(ReflectionHelper.getFqnType(localName,simpleToFqn,globalImports))
			}
			e.setType(t);

			var i = 0;
			while( i < attributes.length ) {
				if( attributes.getURI(i).isFXNamedSpace ) {
					if( "id".equals(attributes.getLocalName(i)) ) {
						e.name = attributes.getValue(i)
						idMap.put(e.name,e);
					} else if( "controller".equals(attributes.getLocalName(i)) ) {
						val ct = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference()
						ct.type = provider.findTypeByName(ReflectionHelper.getFqnType(attributes.getValue(i),simpleToFqn,globalImports))
						model.componentDef.controller = ct
					}
				} else {
					if( ! attributes.getLocalName(i).contains('.') ) {
						if( attributes.getValue(i).startsWith("%") ) {
							val vp = FXGraphFactory.eINSTANCE.createResourceValueProperty()
							val sv = FXGraphFactory.eINSTANCE.createStringValue;
							sv.value = attributes.getValue(i).substring(1)
							vp.setValue(sv);

							val pp = FXGraphFactory.eINSTANCE.createProperty
							pp.name = attributes.getLocalName(i)
							pp.value = vp
							e.properties += pp
						} else if( attributes.getValue(i).startsWith("@") ) {
							val vp = FXGraphFactory.eINSTANCE.createLocationValueProperty()
							vp.value = attributes.getValue(i).substring(1);

							val pp = FXGraphFactory.eINSTANCE.createProperty
							pp.name = attributes.getLocalName(i)
							pp.value = vp
							e.properties += pp
						} else if(attributes.getValue(i).startsWith("$")) {
							val v = attributes.getValue(i).substring(1);
							var Element ref = idMap.get(v)

							if( ref != null ) {
								val vp = FXGraphFactory.eINSTANCE.createReferenceValueProperty
								vp.setReference(ref as Element);
								val pp = FXGraphFactory.eINSTANCE.createProperty
								pp.name = attributes.getLocalName(i)
								pp.value = vp
								e.properties += pp
							}
						} else {
							if( isDynRoot && "type" == attributes.getLocalName(i) ) {
								// Skip this attribute
							} else if( "javafx.scene.image.Image" == t.type.qualifiedName ) {
								val attname = attributes.getLocalName(i)
								val vp = FXGraphFactory.eINSTANCE.createSimpleValueProperty
								if( "requestedWidth" ==  attname || "requestedHeight" == attname ) {
									vp.number = attributes.getValue(i)
								} else if( "preserveRatio" == attname || "smooth" == attname || "backgroundLoading" == attname ) {
									vp.booleanValue = attributes.getValue(i)
								}
								val pp = FXGraphFactory.eINSTANCE.createProperty
								pp.name = attributes.getLocalName(i)
								pp.value = vp
								e.properties += pp
//							} else if( "javafx.scene.shape.TriangleMesh" == t.type.qualifiedName ) {
//
							} else {
								val vt = ReflectionHelper.getValueType(t.type,attributes.getLocalName(i))

								if( vt == ValueType.EVENT_CLASS ) {
									val vp = FXGraphFactory.eINSTANCE.createControllerHandledValueProperty
									vp.setMethodname(attributes.getValue(i).substring(1))

									val pp = FXGraphFactory.eINSTANCE.createProperty
									pp.name = attributes.getLocalName(i)
									pp.value = vp
									e.properties += pp

								} else if( vt == ValueType.LIST ) {
									val vp = FXGraphFactory.eINSTANCE.createListValueProperty

									attributes.getValue(i).split(",").forEach[v |
										val lv = FXGraphFactory.eINSTANCE.createSimpleValueProperty
										lv.stringValue = v
										vp.value += lv
									]


									val pp = FXGraphFactory.eINSTANCE.createProperty
									pp.name = attributes.getLocalName(i)
									pp.value = vp
									e.properties += pp
								} else {
									val vp = FXGraphFactory.eINSTANCE.createSimpleValueProperty
									if( vt == ValueType.BOOLEAN ) {
										vp.booleanValue = attributes.getValue(i)
									} else if( vt == ValueType.NUMBER ) {
										vp.number = attributes.getValue(i)
									} else {
										vp.stringValue = attributes.getValue(i)
									}

									val pp = FXGraphFactory.eINSTANCE.createProperty
									pp.name = attributes.getLocalName(i)
									pp.value = vp
									e.properties += pp
								}
							}
						}
					} else {
						if( attributes.getValue(i).startsWith("$") ) {
							val v = attributes.getValue(i).substring(1);
							var Element ref = idMap.get(v)

							if(ref != null) {
								val vp = FXGraphFactory.eINSTANCE.createReferenceValueProperty
								vp.setReference(ref as Element);
								val pp = FXGraphFactory.eINSTANCE.createStaticCallValueProperty
								val ttype = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference()
								val idx = attributes.getLocalName(i).indexOf('.')
								val type = provider.findTypeByName(ReflectionHelper.getFqnType(attributes.getLocalName(i).substring(0,idx),simpleToFqn,globalImports))
								ttype.type = type
								pp.setType(ttype)
								pp.name = attributes.getLocalName(i).substring(attributes.getLocalName(i).indexOf('.')+1)
								pp.value = vp
								e.staticCallProperties += pp
							}
						} else {
							val idx = attributes.getLocalName(i).indexOf('.')
							val type = provider.findTypeByName(ReflectionHelper.getFqnType(attributes.getLocalName(i).substring(0,idx),simpleToFqn,globalImports))

							val vt = ReflectionHelper.getStaticValueType(type,attributes.getLocalName(i).substring(idx+1))
							val vp = FXGraphFactory.eINSTANCE.createSimpleValueProperty
							if( vt == ValueType.BOOLEAN ) {
								vp.booleanValue = attributes.getValue(i)
							} else if( vt == ValueType.NUMBER ) {
								vp.number = attributes.getValue(i)
							} else {
								vp.stringValue = attributes.getValue(i)
							}

							val pp = FXGraphFactory.eINSTANCE.createStaticCallValueProperty
							val ttype = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference()
							ttype.type = type
							pp.setType(ttype)
							pp.name = attributes.getLocalName(i).substring(attributes.getLocalName(i).indexOf('.')+1)
							pp.value = vp
							e.staticCallProperties += pp
						}
					}
				}

				i = i +1;
			}

			if( model.componentDef.rootNode == null ) {
				model.componentDef.rootNode = e
			}

			stack.push(e)
		}
	}

	override endElement(String uri, String localName, String qName) throws SAXException {
		if( valueOfType ) {
			valueOfType = false;
			return;
		}
		val o = stack.pop
		if( ! stack.isEmpty && o instanceof Element ) {
			if( stack.peek instanceof Element ) {
				(stack.peek as Element).defaultChildren += o as Element
			} if( stack.peek instanceof StaticCallValueProperty ) {
				(stack.peek as StaticCallValueProperty).setValue(o as Element)
			} else if( stack.peek instanceof Property ) {
				val p = stack.peek as Property
				if( p.value == null ) {
					p.value = o as Element
				} else {
					(p.value as ListValueProperty).value += o as Element
				}
			}
		}
	}
}