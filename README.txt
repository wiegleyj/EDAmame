EDAmame is yet another EDA design program.

Why? Several reasons:
    1) Most EDA software is proprietary and expensive.
        * Altium Designer for example is prohibitively expensive
        * CircuitMaker is free (by Altium) but forces your designs to be public and your
          data is stored on their cloud and at their mercy.
        * Eagle and others have unacceptable restrictions on project sizes in their "free" versions.
        * EasyEDA is an online/web app and thus slow and severely limited in functionality.
    2) The open source EDA solutions are, honestly, terrible from an intuitive GUI stand point.
       KiCAD is the most popular. It is almost the worst UI I've ever used. If you've used
       Altium you will find KiCAD unbearable.
    3) Cross platform support. EDAmame is written in Java using the JavaFX GUI framework.
       It will run everywhere. period.
    4) MOST importantly. No other product represents symbols/parts/vendors/footprints correctly.
       Altium Designer forces you to make a seperate copy of the resistor symbol for every single
       resistor value. in the 1% 0603 resistors that means you maintain over 100 different symbols.
       Need to change something small about the "resistor" symbol means you have to change all 100
       "parts" because altium really doesn't use "symbols", they use parts.
       KiCAD? Sure, one symbol. But you don't assign parts in the schematic, you assign them later
       as part of the BOM.  This is all incredibly insane. The values and atributes of components
       are part of the schemtic. The schematic is the circuit design, not the BOM or the PCB. KiCAD
       also makes the mistake of "mixing" all libraries into a sea of confusion.
    5) Building a reusable library of parts in these existing systems is a nightmare. It takes days
       to build up an Altium parts library for a single project. There's no reason we can't all be
       using a single central library parts crowd sourced.

Here's the biggest reason the project exists.... Existing tools handle parts all wrong.

EDAmame's solution to symbols/parts/vendors/footprints:
1) Symbol library. A symbol library is a collection of symbols. Symbols are not duplicated.
   All resistors share the same symbol regardless of value, manufacturer, or vendors. You never
   duplicate the same graphical representation. Though you can have two different symbols to
   represent the same design concept, such as the older zigzag resistor symbol vs the more
   modern rectangular version. The key here is that they graphically different in some way.
   Symbols are intended to be created/modified through a graphical editor as part of EDAmame.
   Similar to Altium's SchLib files/editor or KiCAD's symbol library.

2) Footprint library. Same, you never create a duplicate footprint. The other EDA solutions get
   this right at least. Footprints are intended to be created/modified through a graphical editor
   as part of EDAmame. Similar to Altium's PcbLib files/editor or KiCAD's footprint library.

3) Parts. A part is an actual device that you buy and physically incorporate onto the final PCB.
   Parts have attributes, such as resistance, tolerance, forward voltage drop, etc. This is
   where you define "multiple" resistors. Every resistor value will be a different part.

   Parts are associated with one or more symbols. When you add 220Ohm 1% 0603 resistor you would
   likely assign it two symbols (the zigzig resistor symbol and the rectangle resistor symbol).

   Parts are associated with one or more manufacturers. Manufacturers are the companies that
   manufacture parts. (Sometimes different companies manufacturer the same part to the same
   specifications. If they share the same manufacturer part number and specifications, then
   they are the same part. Examples are the 74series logic parts.

   Parts with the same functionality but different packages (DIP vs SOIC) are different parts.

   Parts are associated with one or more footprints. Multiple footprints are needed because of
   different density designs. An 0603 resistor may have different pad sizes and courtyard spacing
   based on the PCB manufacturer's capabilities or the intended reliability of production. It may
   be desirable to have different 0603 footprints such as rectangular pads vs rounded rectangular
   pads to prevent tombstoning during reflow soldering. Thus footprints are not unique to parts.

   Parts are associated with one or more vendors. Vendors sell you the parts. Many vendors sell
   the same parts. For each part we can associate a variety of vendors and vendor order numbers
   to obtain the parts.

   Parts are intended to be created or edited either through a graphical database editor in
   EDAmame but of equal importance is the ability to create/modify parts in external tools such
   as Microsoft Excel/notepad/CVS and uploaded as huge batches. The idea... make a couple of
   symbols, a couple of footprints and then upload a spreadsheet of every single resistor that
   Digikey, Mouser, LCSC, Newark sells. Instantly establishing thousands of available parts that
   can be easily filtered and used by all projects and by thousands of designers. Putting an end
   to the insanity that other tools impose on users when creating/updating flexible/reusable
   libraries. No more assigning specific parts/vendors to a symbol such as done in Altium. No more
   forcing users to respecify the same parts at BOM creation for different projects such as KiCAD.

4) Vendors. Many modern vendors maintain sales APIs. EDAmame is intended to support automatic BOM
   and part cross-referencing and procurement through these vendors with ease.

One library, You place symbols on the schematic, you specify contraints on the solutions to those
symbols... actual parts are selected automatically for production. The BOM can be fine tuned after
the design and PCB are completed and prior to manufacture.

EDAmame intends the design flow to be:
A) Users select and place symbols on the schematic to represent needed components.
B) Symbols can be double-clicked to select and specify constraints on the symbol's solution as part
   of the electronic design captured by the schematic.
   Values for attributes such as tolerance, resistance, current limits, etc. can be specified
   as either exact values (typical of resistors) or as ranges.
   A specific footprint would be specified. (Such as 0805). A specific footprint is important
   because this is what is pushed to the PCB design and is difficult to change later.
   The BOM would automatically list all available parts that meet the solution. The available
   parts should end up being interchangeable. Different manufacturers, different vendors possible
   but same performance specifications and footprint compatibility.
C) Design is pushed to the PCB and routed.
D) BOM is automatic, Vendor pricing and availability is automated.

Summary of main features:
1) True cross-platform compatibility. EDAmame will run the same on all JVM capable machines.
2) Sane, highly reusable symbol/part/footprint libraries.
3) Efficient easy scalability of part libraries.
4) Automatic BOM generation and part cross-referencing.
5) Extremely intuitive UI. Powerful UI features available but unintrusive.
6) Utilization of either local or remote symbol/part/footprint libraries.
7) Support for Vendor pricing/availability APIs.
8) Excellent and precise alignment and placement of graphical elements.
9) Robust location, placement origins, grids, and snap systems.
10) Accessible and non-proprietary data formats.
11) Automated/intelligent schematic and board annotation.
