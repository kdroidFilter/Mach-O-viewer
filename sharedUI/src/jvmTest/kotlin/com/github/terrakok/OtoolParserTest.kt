package com.github.terrakok

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtoolParserTest {
    @Test
    fun testParseOttolFile() {
        val input = this::class.java.getResource("/otool-witr.txt")?.readText()
            ?: error("Resource not found")
        val machOFile = OtoolParser.parse(input)
        assertEquals("/opt/homebrew/bin/witr", machOFile.path)
        assertEquals(19, machOFile.loadCommands.size) // Based on looking at the file: 0..18

        val lc18 = machOFile.loadCommands.last() as LinkeditDataCommand
        assertEquals("LC_CODE_SIGNATURE", lc18.cmd)
        assertEquals(6122272, lc18.dataoff)
        assertEquals(47954, lc18.datasize)
    }

    @Test
    fun testParseFullOtoolOutput() {
        val input = """
/opt/homebrew/bin/witr:
Load command 0
      cmd LC_SEGMENT_64
  cmdsize 72
  segname __PAGEZERO
   vmaddr 0x0000000000000000
   vmsize 0x0000000100000000
  fileoff 0
 filesize 0
  maxprot ---
 initprot ---
   nsects 0
    flags (none)
Load command 1
      cmd LC_SEGMENT_64
  cmdsize 792
  segname __TEXT
   vmaddr 0x0000000100000000
   vmsize 0x00000000004cc000
  fileoff 0
 filesize 5029888
  maxprot r-x
 initprot r-x
   nsects 9
    flags (none)
Section
  sectname __text
   segname __TEXT
      addr 0x00000001000016a0
      size 0x0000000000246b90
    offset 5792
     align 2^4 (16)
    reloff 0
    nreloc 0
      type S_REGULAR
attributes PURE_INSTRUCTIONS SOME_INSTRUCTIONS
 reserved1 0
 reserved2 0
Section
  sectname __stubs
   segname __TEXT
      addr 0x0000000100248230
      size 0x0000000000000564
    offset 2392624
     align 2^2 (4)
    reloff 0
    nreloc 0
      type S_SYMBOL_STUBS
attributes PURE_INSTRUCTIONS SOME_INSTRUCTIONS
 reserved1 0 (index into indirect symbol table)
 reserved2 12 (size of stubs)
Load command 5
            cmd LC_DYLD_INFO_ONLY
        cmdsize 48
     rebase_off 6078464
    rebase_size 22520
       bind_off 6100984
      bind_size 64
  weak_bind_off 0
 weak_bind_size 0
  lazy_bind_off 6101048
 lazy_bind_size 2152
     export_off 6103200
    export_size 880
Load command 6
     cmd LC_SYMTAB
 cmdsize 24
  symoff 6116608
   nsyms 152
  stroff 6119976
 strsize 2288
Load command 7
            cmd LC_DYSYMTAB
        cmdsize 80
      ilocalsym 0
      nlocalsym 0
     iextdefsym 0
     nextdefsym 34
      iundefsym 34
      nundefsym 118
         tocoff 0
           ntoc 0
      modtaboff 0
        nmodtab 0
   extrefsymoff 0
    nextrefsyms 0
 indirectsymoff 6119040
  nindirectsyms 233
      extreloff 0
        nextrel 0
      locreloff 0
        nlocrel 0
Load command 8
          cmd LC_LOAD_DYLINKER
      cmdsize 32
         name /usr/lib/dyld (offset 12)
Load command 9
     cmd LC_UUID
 cmdsize 24
    uuid 9983F3E2-9F21-392A-FD4D-8C527E528233
Load command 10
      cmd LC_BUILD_VERSION
  cmdsize 32
 platform MACOS
    minos 11.0
      sdk 15.4
   ntools 1
     tool LD
  version 1230.1
Load command 11
      cmd LC_SOURCE_VERSION
  cmdsize 16
  version 0.0
Load command 12
       cmd LC_MAIN
   cmdsize 24
  entryoff 537968
 stacksize 0
Load command 13
          cmd LC_LOAD_DYLIB
      cmdsize 56
         name /usr/lib/libresolv.9.dylib (offset 24)
   time stamp 2 Thu Jan  1 01:00:02 1970
      current version 1.0.0
compatibility version 1.0.0
Load command 16
      cmd LC_FUNCTION_STARTS
  cmdsize 16
  dataoff 6104080
 datasize 12528
        """.trimIndent()

        val machOFile = OtoolParser.parse(input)
        assertEquals("/opt/homebrew/bin/witr", machOFile.path)
        
        val lc0 = machOFile.loadCommands.find { it.cmd == "LC_SEGMENT_64" && (it as SegmentCommand).segname == "__PAGEZERO" } as SegmentCommand
        assertEquals(72, lc0.cmdSize)
        assertEquals(0, lc0.nsects)

        val lc1 = machOFile.loadCommands.find { it.cmd == "LC_SEGMENT_64" && (it as SegmentCommand).segname == "__TEXT" } as SegmentCommand
        assertEquals(2, lc1.sections.size) // We only included 2 sections in the input above
        assertEquals("__text", lc1.sections[0].sectname)
        assertEquals("PURE_INSTRUCTIONS SOME_INSTRUCTIONS", lc1.sections[0].attributes)
        assertEquals("0 (index into indirect symbol table)", lc1.sections[1].reserved1)
        assertEquals("12 (size of stubs)", lc1.sections[1].reserved2)

        val lc5 = machOFile.loadCommands.find { it.cmd == "LC_DYLD_INFO_ONLY" } as DyldInfoCommand
        assertEquals(6078464, lc5.rebaseOff)
        assertEquals(880, lc5.exportSize)

        val lc6 = machOFile.loadCommands.find { it.cmd == "LC_SYMTAB" } as SymtabCommand
        assertEquals(6116608, lc6.symoff)
        assertEquals(152, lc6.nsyms)

        val lc7 = machOFile.loadCommands.find { it.cmd == "LC_DYSYMTAB" } as DysymtabCommand
        assertEquals(80, lc7.cmdSize)
        assertEquals(233, lc7.nindirectsyms)

        val lc8 = machOFile.loadCommands.find { it.cmd == "LC_LOAD_DYLINKER" } as DylinkerCommand
        assertEquals("/usr/lib/dyld", lc8.name)

        val lc9 = machOFile.loadCommands.find { it.cmd == "LC_UUID" } as UuidCommand
        assertEquals("9983F3E2-9F21-392A-FD4D-8C527E528233", lc9.uuid)

        val lc10 = machOFile.loadCommands.find { it.cmd == "LC_BUILD_VERSION" } as BuildVersionCommand
        assertEquals("MACOS", lc10.platform)
        assertEquals("11.0", lc10.minos)
        assertEquals("15.4", lc10.sdk)
        assertEquals(1, lc10.ntools)
        assertEquals("LD", lc10.tools[0].tool)
        assertEquals("1230.1", lc10.tools[0].version)

        val lc11 = machOFile.loadCommands.find { it.cmd == "LC_SOURCE_VERSION" } as SourceVersionCommand
        assertEquals("0.0", lc11.version)

        val lc12 = machOFile.loadCommands.find { it.cmd == "LC_MAIN" } as MainCommand
        assertEquals(537968, lc12.entryoff)
        assertEquals(0, lc12.stacksize)

        val lc13 = machOFile.loadCommands.find { it.cmd == "LC_LOAD_DYLIB" } as DylibCommand
        assertEquals("/usr/lib/libresolv.9.dylib", lc13.name)
        assertEquals("1.0.0", lc13.currentVersion)

        val lc16 = machOFile.loadCommands.find { it.cmd == "LC_FUNCTION_STARTS" } as LinkeditDataCommand
        assertEquals("LC_FUNCTION_STARTS", lc16.cmd)
        assertEquals(6104080, lc16.dataoff)
        assertEquals(12528, lc16.datasize)
    }
}
