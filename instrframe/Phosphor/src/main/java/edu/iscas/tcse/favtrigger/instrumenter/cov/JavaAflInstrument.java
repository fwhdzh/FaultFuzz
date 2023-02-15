/**
 * Copyright 2018  Jussi Judin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.iscas.tcse.favtrigger.instrumenter.cov;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.iscas.tcse.favtrigger.instrumenter.CoverageMap;

import static org.objectweb.asm.Opcodes.*;

public class JavaAflInstrument {
	public static int total_locations = 0;
	private static int total_jarfiles = 0;
	private static int total_classfiles = 0;

    public static class InstrumentationOptions {
        int ratio;
        boolean has_custom_init;
        public boolean deterministic;

        public InstrumentationOptions(int ratio_, boolean has_custom_init_, boolean deterministic_) {
            ratio = ratio_;
            has_custom_init = has_custom_init_;
            deterministic = deterministic_;
        }

        InstrumentationOptions(InstrumentationOptions other) {
            this(other.ratio, other.has_custom_init, other.deterministic);
        }
}

    public static class InstrumentingMethodVisitor extends MethodVisitor {
        private boolean _has_custom_init;
        private int _instrumentation_ratio;
        private boolean _is_main;
        private InstrumentationOptions _options;
        private Random _random;
        private boolean _aflInst;
        private String _cname;
        private String _mname;
        private Set<Label> blockStartLables;
        private boolean after_jump_before_label;
        private boolean after_block_before_mark;
        private boolean skip_jump_between_switch;

        public InstrumentingMethodVisitor(
            MethodVisitor mv_,
            Random random,
            InstrumentationOptions options,
            boolean is_main,
            boolean aflInst,
            String cname,
            String mname) {
            super(Configuration.ASM_VERSION, mv_);
            _is_main = is_main;
            _random = random;
            _instrumentation_ratio = options.ratio;
            _has_custom_init = options.has_custom_init;
            _aflInst = aflInst;
            _cname = cname;
            _mname = mname;
            this.blockStartLables = new HashSet<Label>();
            after_jump_before_label = false;
            after_block_before_mark = false;
            skip_jump_between_switch = false;
        }

        private void _aflMaybeLog() {
            JavaAflInstrument.total_locations++;
            int location_id = _random.nextInt(JavaAfl.map.length);
            // + &JavaAfl.map
            mv.visitFieldInsn(GETSTATIC, "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl", "map", "[B");

            // + location_id
            mv.visitLdcInsn(location_id);
            // + JavaAfl.prev_location
            mv.visitFieldInsn(GETSTATIC, "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl", "prev_location", "I");

            mv.visitInsn(DUP);
            mv.visitLdcInsn(location_id);
            mv.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl", "mark", "(II)V", false);

            // - 2 values (location_id, prev_location)
            // + location_id ^ prev_location -> tuple_index
            mv.visitInsn(IXOR);

            // + &JavaAfl.map
            // + tuple_index
            mv.visitInsn(DUP2);
            // - 2 values (&JavaAfl.map, tuple_index)
            // + JavaAfl.map[tuple_index] -> previous_tuple_value
            mv.visitInsn(BALOAD);
            // + 1
            mv.visitInsn(ICONST_1);
            // - 2 values (1, previous_tuple_value)
            // + 1 + previous_tuple_value -> new_tuple_value
            mv.visitInsn(IADD);
            // = (byte)new_tuple_value
            mv.visitInsn(I2B);
            // - 3 values (new_tuple_value, tuple_index, &JavaAfl.map)
            // = new_tuple_value
            mv.visitInsn(BASTORE);
            // Stack modifications are now +-0 here.

            // + location_id >> 1 = shifted_location
            mv.visitLdcInsn(location_id >> 1);
            // - 1 value (shifted_location)
            mv.visitFieldInsn(PUTSTATIC, "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl", "prev_location", "I");
        }

        private void _gyMaybeLog() {
        	if(this._cname.startsWith("org/apache/zookeeper/server/NIOServerCnxnFactory")) {
//        					&& this._mname.startsWith("chooseTarget4NewBlock"))) {//and verifyReplication
        		return;
        	}
        	JavaAflInstrument.total_locations++;
            int location_id = _random.nextInt(JavaAfl.map.length);

            // + &JavaAfl.map
            mv.visitFieldInsn(GETSTATIC, "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl", "map", "[B");

//            mv.visitLdcInsn(location_id);
//            mv.visitLdcInsn(location_id);
//            mv.visitLdcInsn(this._cname+" "+this._mname);
//            mv.visitMethodInsn(INVOKESTATIC, "javafl/JavaAfl", "mark", "(IILjava/lang/String;)V", false);

            // + location_id
            mv.visitLdcInsn(location_id);
            mv.visitInsn(DUP2);
            mv.visitInsn(BALOAD);
            // + 1
            mv.visitInsn(ICONST_1);
            // - 2 values (1, previous_tuple_value)
            // + 1 + previous_tuple_value -> new_tuple_value
            mv.visitInsn(IADD);
            // = (byte)new_tuple_value
            mv.visitInsn(I2B);
            // - 3 values (new_tuple_value, tuple_index, &JavaAfl.map)
            // = new_tuple_value
            mv.visitInsn(BASTORE);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            if (_is_main && !_has_custom_init) {
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl",
                    "_before_main",
                    "()V",
                    false);
            }
            if ((_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
            	_gyMaybeLog();
            }
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            mv.visitJumpInsn(opcode, label);
            if(skip_jump_between_switch) {
            	return;
            }
            this.blockStartLables.add(label);
            if(opcode != Opcodes.GOTO) {
            	after_jump_before_label = true;//a visitLabel can follow the jump, or other "normal" insts can also follow the jump
            }
       }

        @Override
        public void visitLabel(Label label) {
        	if(after_jump_before_label && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
//        		after_jump_before_label = false;
//        		Label new_label = new Label();
//            	super.visitLabel(new_label);
//				_gyMaybeLog();
        	}

            mv.visitLabel(label);
            if(after_jump_before_label) {
            	this.blockStartLables.add(label);
                after_jump_before_label = false;
                after_block_before_mark = true;
            } else if (isABlockLabel(label)) {
            	after_block_before_mark = true;
            } else if (after_block_before_mark) {
            	this.blockStartLables.add(label);
            }
//            _aflMaybeLog();
        }

        public boolean isABlockLabel(Label label) {
        	for(Label l:blockStartLables) {
        		if(l.equals(label)) {
        			return true;
        		}
        	}
        	return false;
        }

        @Override
		public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
			// TODO Auto-generated method stub
			super.visitFrame(type, numLocal, local, numStack, stack);
//			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
//				_gyMaybeLog();
//				after_block_before_mark = false;
//            }
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitVarInsn(opcode, var);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			// TODO Auto-generated method stub
			if (opcode != Opcodes.NEW && after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitTypeInsn(opcode, type);
			if (opcode == Opcodes.NEW && after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitFieldInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitMethodInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
				Object... bootstrapMethodArguments) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
		}

		@Override
		public void visitLdcInsn(Object value) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitLdcInsn(value);
		}

		@Override
		public void visitIincInsn(int var, int increment) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
			// TODO Auto-generated method stub
			skip_jump_between_switch = false;
			super.visitTableSwitchInsn(min, max, dflt, labels);
			this.blockStartLables.add(dflt);
			for(Label l:labels) {
				this.blockStartLables.add(l);
			}
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			// TODO Auto-generated method stub
			super.visitLookupSwitchInsn(dflt, keys, labels);
			skip_jump_between_switch = true;
		}

		@Override
		public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
			// TODO Auto-generated method stub
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
			super.visitMultiANewArrayInsn(descriptor, numDimensions);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			// TODO Auto-generated method stub
			super.visitTryCatchBlock(start, end, handler, type);
			this.blockStartLables.add(start);
			this.blockStartLables.add(handler);
		}

		@Override
        public void visitInsn(int opcode) {
            // Main gets special treatment in handling returns. It
            // can't return anything else than void:
			if (after_block_before_mark && (_random.nextInt(100) < _instrumentation_ratio) && _aflInst) {
				_gyMaybeLog();
				after_block_before_mark = false;
            }
            if (_is_main && opcode == RETURN) {
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "edu/iscas/tcse/favtrigger/instrumenter/cov/JavaAfl",
                    "_after_main",
                    "()V",
                    false);
            }
            mv.visitInsn(opcode);
        }

    }

    public static class InstrumentingClassVisitor extends ClassVisitor {
        ClassWriter _writer;
        InstrumentationOptions _options;
        Random _random;
        String _className;

        public InstrumentingClassVisitor(
            ClassVisitor _cv,
            Random random,
            InstrumentationOptions options, String className) {
            super(Configuration.ASM_VERSION, _cv);
//            _writer = (ClassWriter) _cv;
            _random = random;
            _options = options;
            _className = className;
        }

        @Override
        public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(
                access, name, desc, signature, exceptions);
            if (mv == null) {
                return null;
            }

            //check afl_allowlist and afl_denylist
            boolean aflInst = CoverageMap.useAFLInst(_className, name, desc);
//            if(aflInst) {
//            	System.out.println("useAFLInst "+_className+" "+name+" "+aflInst);
//            }

            // Instrument all public static main functions with the
            // start-up and teardown instrumentation.
            int public_static = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
            if (name.equals("main") && ((access & public_static) != 0)) {
//                _writer.newMethod(
//                    "javafl/JavaAfl",
//                    "_before_main",
//                    "()V",
//                    false);
//                _writer.newMethod(
//                    "javafl/JavaAfl",
//                    "_after_main",
//                    "()V",
//                    false);
                mv = new InstrumentingMethodVisitor(
                    mv, _random, _options, true, aflInst, _className, name);
            } else {
                mv = new InstrumentingMethodVisitor(
                    mv, _random, _options, false, aflInst, _className, name);
            }
            return mv;
        }
    }

    private static boolean is_instrumented(ClassReader reader) {
        // It would be sooo much more easy if Java had memmem() like
        // function in its standard library...
        int items = reader.getItemCount();
        byte[] marker_bytes = edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl.INSTRUMENTATION_MARKER.getBytes();
        for (int i = 0; i < items; i++) {
            int index = reader.getItem(i);
            int item_size = reader.b[index] * 256 + reader.b[index + 1];
            if (item_size != marker_bytes.length) {
                continue;
            }
            int start = index + 2;
            int end = start + marker_bytes.length;
            if (reader.b.length < end) {
                return false;
            }
            byte[] value = Arrays.copyOfRange(reader.b, start, end);
            if (Arrays.equals(marker_bytes, value)) {
                return true;
            }
        }
        return false;
    }

    private static byte[] input_stream_to_bytes(InputStream stream) {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            int read = stream.read(buffer);
            while (read > 0) {
                bytestream.write(buffer, 0, read);
                read = stream.read(buffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytestream.toByteArray();
    }

}
