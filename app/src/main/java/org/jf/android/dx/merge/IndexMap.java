/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jf.android.dx.merge;

import org.jf.android.dex.Annotation;
import org.jf.android.dex.CallSiteId;
import org.jf.android.dex.ClassDef;
import org.jf.android.dex.Dex;
import org.jf.android.dex.DexException;
import org.jf.android.dex.EncodedValue;
import org.jf.android.dex.EncodedValueCodec;
import org.jf.android.dex.EncodedValueReader;
import org.jf.android.dex.FieldId;
import org.jf.android.dex.Leb128;
import org.jf.android.dex.MethodHandle;
import org.jf.android.dex.MethodId;
import org.jf.android.dex.ProtoId;
import org.jf.android.dex.TableOfContents;
import org.jf.android.dex.TypeList;
import org.jf.android.dex.util.ByteOutput;
import org.jf.android.dx.util.ByteArrayAnnotatedOutput;

import java.util.HashMap;

/**
 * Maps the index offsets from one dex file to those in another. For example, if
 * you have string #5 in the old dex file, its position in the new dex file is
 * {@code strings[5]}.
 */
public final class IndexMap {
    private final Dex target;
    public final int[] stringIds;
    public final short[] typeIds;
    public final short[] protoIds;
    public final short[] fieldIds;
    public final short[] methodIds;
    public final int[] callSiteIds;
    public final HashMap<Integer, Integer> methodHandleIds;
    private final HashMap<Integer, Integer> typeListOffsets;
    private final HashMap<Integer, Integer> annotationOffsets;
    private final HashMap<Integer, Integer> annotationSetOffsets;
    private final HashMap<Integer, Integer> annotationSetRefListOffsets;
    private final HashMap<Integer, Integer> annotationDirectoryOffsets;
    private final HashMap<Integer, Integer> encodedArrayValueOffset;

    public IndexMap(Dex target, TableOfContents tableOfContents) {
        this.target = target;
        this.stringIds = new int[tableOfContents.stringIds.size];
        this.typeIds = new short[tableOfContents.typeIds.size];
        this.protoIds = new short[tableOfContents.protoIds.size];
        this.fieldIds = new short[tableOfContents.fieldIds.size];
        this.methodIds = new short[tableOfContents.methodIds.size];
        this.callSiteIds = new int[tableOfContents.callSiteIds.size];
        this.methodHandleIds = new HashMap<Integer, Integer>();
        this.typeListOffsets = new HashMap<Integer, Integer>();
        this.annotationOffsets = new HashMap<Integer, Integer>();
        this.annotationSetOffsets = new HashMap<Integer, Integer>();
        this.annotationSetRefListOffsets = new HashMap<Integer, Integer>();
        this.annotationDirectoryOffsets = new HashMap<Integer, Integer>();
        this.encodedArrayValueOffset = new HashMap<Integer, Integer>();

        /*
         * A type list, annotation set, annotation directory, or static value at
         * offset 0 is always empty. Always map offset 0 to 0.
         */
        this.typeListOffsets.put(0, 0);
        this.annotationSetOffsets.put(0, 0);
        this.annotationDirectoryOffsets.put(0, 0);
        this.encodedArrayValueOffset.put(0, 0);
    }

    public void putTypeListOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        typeListOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationSetOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationSetOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationSetRefListOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationSetRefListOffsets.put(oldOffset, newOffset);
    }

    public void putAnnotationDirectoryOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        annotationDirectoryOffsets.put(oldOffset, newOffset);
    }

    public void putEncodedArrayValueOffset(int oldOffset, int newOffset) {
        if (oldOffset <= 0 || newOffset <= 0) {
            throw new IllegalArgumentException();
        }
        encodedArrayValueOffset.put(oldOffset, newOffset);
    }

    public int adjustString(int stringIndex) {
        return stringIndex == ClassDef.NO_INDEX ? ClassDef.NO_INDEX : stringIds[stringIndex];
    }

    public int adjustType(int typeIndex) {
        return (typeIndex == ClassDef.NO_INDEX) ? ClassDef.NO_INDEX : (typeIds[typeIndex] & 0xffff);
    }

    public TypeList adjustTypeList(TypeList typeList) {
        if (typeList == TypeList.EMPTY) {
            return typeList;
        }
        short[] types = typeList.getTypes().clone();
        for (int i = 0; i < types.length; i++) {
            types[i] = (short) adjustType(types[i]);
        }
        return new TypeList(target, types);
    }

    public int adjustProto(int protoIndex) {
        return protoIds[protoIndex] & 0xffff;
    }

    public int adjustField(int fieldIndex) {
        return fieldIds[fieldIndex] & 0xffff;
    }

    public int adjustMethod(int methodIndex) {
        return methodIds[methodIndex] & 0xffff;
    }

    public int adjustTypeListOffset(int typeListOffset) {
        return typeListOffsets.get(typeListOffset);
    }

    public int adjustAnnotation(int annotationOffset) {
        return annotationOffsets.get(annotationOffset);
    }

    public int adjustAnnotationSet(int annotationSetOffset) {
        return annotationSetOffsets.get(annotationSetOffset);
    }

    public int adjustAnnotationSetRefList(int annotationSetRefListOffset) {
        return annotationSetRefListOffsets.get(annotationSetRefListOffset);
    }

    public int adjustAnnotationDirectory(int annotationDirectoryOffset) {
        return annotationDirectoryOffsets.get(annotationDirectoryOffset);
    }

    public int adjustEncodedArray(int encodedArrayAttribute) {
        return encodedArrayValueOffset.get(encodedArrayAttribute);
    }

    public int adjustCallSite(int callSiteIndex) {
        return callSiteIds[callSiteIndex];
    }

    public int adjustMethodHandle(int methodHandleIndex) {
        return methodHandleIds.get(methodHandleIndex);
    }

    public MethodId adjust(MethodId methodId) {
        return new MethodId(target,
                adjustType(methodId.getDeclaringClassIndex()),
                adjustProto(methodId.getProtoIndex()),
                adjustString(methodId.getNameIndex()));
    }

    public CallSiteId adjust(CallSiteId callSiteId) {
        return new CallSiteId(target, adjustEncodedArray(callSiteId.getCallSiteOffset()));
    }

    public MethodHandle adjust(MethodHandle methodHandle) {
        return new MethodHandle(
                target,
                methodHandle.getMethodHandleType(),
                methodHandle.getUnused1(),
                methodHandle.getMethodHandleType().isField()
                        ? adjustField(methodHandle.getFieldOrMethodId())
                        : adjustMethod(methodHandle.getFieldOrMethodId()),
                methodHandle.getUnused2());
    }

    public FieldId adjust(FieldId fieldId) {
        return new FieldId(target,
                adjustType(fieldId.getDeclaringClassIndex()),
                adjustType(fieldId.getTypeIndex()),
                adjustString(fieldId.getNameIndex()));

    }

    public ProtoId adjust(ProtoId protoId) {
        return new ProtoId(target,
                adjustString(protoId.getShortyIndex()),
                adjustType(protoId.getReturnTypeIndex()),
                adjustTypeListOffset(protoId.getParametersOffset()));
    }

    public ClassDef adjust(ClassDef classDef) {
        return new ClassDef(target, classDef.getOffset(), adjustType(classDef.getTypeIndex()),
                classDef.getAccessFlags(), adjustType(classDef.getSupertypeIndex()),
                adjustTypeListOffset(classDef.getInterfacesOffset()), classDef.getSourceFileIndex(),
                classDef.getAnnotationsOffset(), classDef.getClassDataOffset(),
                classDef.getStaticValuesOffset());
    }

    public SortableType adjust(SortableType sortableType) {
        return new SortableType(sortableType.getDex(),
                sortableType.getIndexMap(), adjust(sortableType.getClassDef()));
    }

    public EncodedValue adjustEncodedValue(EncodedValue encodedValue) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput(32);
        new EncodedValueTransformer(out).transform(new EncodedValueReader(encodedValue));
        return new EncodedValue(out.toByteArray());
    }

    public EncodedValue adjustEncodedArray(EncodedValue encodedArray) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput(32);
        new EncodedValueTransformer(out).transformArray(
                new EncodedValueReader(encodedArray, EncodedValueReader.ENCODED_ARRAY));
        return new EncodedValue(out.toByteArray());
    }

    public Annotation adjust(Annotation annotation) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput(32);
        new EncodedValueTransformer(out).transformAnnotation(
                annotation.getReader());
        return new Annotation(target, annotation.getVisibility(),
                new EncodedValue(out.toByteArray()));
    }

    /**
     * Adjust an encoded value or array.
     */
    private final class EncodedValueTransformer {
        private final ByteOutput out;

        public EncodedValueTransformer(ByteOutput out) {
            this.out = out;
        }

        public void transform(EncodedValueReader reader) {
            // TODO: extract this into a helper class, EncodedValueWriter
            switch (reader.peek()) {
            case EncodedValueReader.ENCODED_BYTE:
                EncodedValueCodec.writeSignedIntegralValue(out, EncodedValueReader.ENCODED_BYTE, reader.readByte());
                break;
            case EncodedValueReader.ENCODED_SHORT:
                EncodedValueCodec.writeSignedIntegralValue(out, EncodedValueReader.ENCODED_SHORT, reader.readShort());
                break;
            case EncodedValueReader.ENCODED_INT:
                EncodedValueCodec.writeSignedIntegralValue(out, EncodedValueReader.ENCODED_INT, reader.readInt());
                break;
            case EncodedValueReader.ENCODED_LONG:
                EncodedValueCodec.writeSignedIntegralValue(out, EncodedValueReader.ENCODED_LONG, reader.readLong());
                break;
            case EncodedValueReader.ENCODED_CHAR:
                EncodedValueCodec.writeUnsignedIntegralValue(out, EncodedValueReader.ENCODED_CHAR, reader.readChar());
                break;
            case EncodedValueReader.ENCODED_FLOAT:
                // Shift value left 32 so that right-zero-extension works.
                long longBits = ((long) Float.floatToIntBits(reader.readFloat())) << 32;
                EncodedValueCodec.writeRightZeroExtendedValue(out, EncodedValueReader.ENCODED_FLOAT, longBits);
                break;
            case EncodedValueReader.ENCODED_DOUBLE:
                EncodedValueCodec.writeRightZeroExtendedValue(
                        out, EncodedValueReader.ENCODED_DOUBLE, Double.doubleToLongBits(reader.readDouble()));
                break;
            case EncodedValueReader.ENCODED_METHOD_TYPE:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_METHOD_TYPE, adjustProto(reader.readMethodType()));
                break;
            case EncodedValueReader.ENCODED_METHOD_HANDLE:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out,
                        EncodedValueReader.ENCODED_METHOD_HANDLE,
                        adjustMethodHandle(reader.readMethodHandle()));
                break;
            case EncodedValueReader.ENCODED_STRING:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_STRING, adjustString(reader.readString()));
                break;
            case EncodedValueReader.ENCODED_TYPE:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_TYPE, adjustType(reader.readType()));
                break;
            case EncodedValueReader.ENCODED_FIELD:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_FIELD, adjustField(reader.readField()));
                break;
            case EncodedValueReader.ENCODED_ENUM:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_ENUM, adjustField(reader.readEnum()));
                break;
            case EncodedValueReader.ENCODED_METHOD:
                EncodedValueCodec.writeUnsignedIntegralValue(
                        out, EncodedValueReader.ENCODED_METHOD, adjustMethod(reader.readMethod()));
                break;
            case EncodedValueReader.ENCODED_ARRAY:
                writeTypeAndArg(EncodedValueReader.ENCODED_ARRAY, 0);
                transformArray(reader);
                break;
            case EncodedValueReader.ENCODED_ANNOTATION:
                writeTypeAndArg(EncodedValueReader.ENCODED_ANNOTATION, 0);
                transformAnnotation(reader);
                break;
            case EncodedValueReader.ENCODED_NULL:
                reader.readNull();
                writeTypeAndArg(EncodedValueReader.ENCODED_NULL, 0);
                break;
            case EncodedValueReader.ENCODED_BOOLEAN:
                boolean value = reader.readBoolean();
                writeTypeAndArg(EncodedValueReader.ENCODED_BOOLEAN, value ? 1 : 0);
                break;
            default:
                throw new DexException("Unexpected type: " + Integer.toHexString(reader.peek()));
            }
        }

        private void transformAnnotation(EncodedValueReader reader) {
            int fieldCount = reader.readAnnotation();
            Leb128.writeUnsignedLeb128(out, adjustType(reader.getAnnotationType()));
            Leb128.writeUnsignedLeb128(out, fieldCount);
            for (int i = 0; i < fieldCount; i++) {
                Leb128.writeUnsignedLeb128(out, adjustString(reader.readAnnotationName()));
                transform(reader);
            }
        }

        private void transformArray(EncodedValueReader reader) {
            int size = reader.readArray();
            Leb128.writeUnsignedLeb128(out, size);
            for (int i = 0; i < size; i++) {
                transform(reader);
            }
        }

        private void writeTypeAndArg(int type, int arg) {
            out.writeByte((arg << 5) | type);
        }
    }
}
