 /*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame;

/**
 * {@link EDAmame} is an Electronic Design Application for the development of electronic circuits and
 * printed circuit boards.<p>
 *
 *     Its primary goals are:
 *     <ul>
 *         <li>Extremely intelligent symbol, footprint, and part libraries.
 *         <ul>
 *             <li>Symbols are never duplicated. Parts in a library can even be linked against symbols
 *             or footprints from another library.</li>
 *             <li>Symbol, footprint, and part libraries are intended to be publicly available for reuse.
 *             Creators or engineers should not be spending their time remaking libraries. Designers
 *             should never need to maintain more than a single library for all designs. (EDAmame still
 *             supports using multiple libraries).</li>
 *             <li>Libraries are implemented using database techniques to improve speed, scalability, and
 *             remote accessibility. EDAmame will be able to support a centralized global library for
 *             all designers. Or you can still create and use your own local libraries.</li>
 *             <li>Footprints and symbols are not parts. EDAmame represents the dependencies between symbols,
 *             schematics, parts, footprints and vendors intelligently and flexibly.
 *             <ul>
 *                 <li>Symbols are abstract representations of high level design concepts. Symbols
 *                 should never be dependent on part, vendor, or footprint details.</li>
 *                 <li>Footprints are a manufacturing requirement specified by a selected part due to its
 *                 physical packaging and shape. Parts specify a footprint but footprints are not dependent
 *                 on part or vendor details.</li>
 *                 <li>Parts are discrete manufactured devices. A part specifies a set of symbols suitable
 *                 to abstractly represent it on schematics. A part has characteristic attributes that
 *                 differentiate it and are used by schematics to constrain acceptable parts based on design
 *                 requirements. A part specifies a set of footprints suitable for PCB manufacturing.</li>
 *             </ul></li>
 *             <li>Parts can be created/added in huge batches with arbitrary attributes. Want to create
 *             different parts for each of the 250,000+ chip resistors provided by DigiKey? No problem;
 *             EDAmame enables you to do that with just a few hours of work (and then nobody else ever has
 *             to do it again).</li>
 *             <li>Library data can be exported in human readable, easily automated or communicated formats
 *             such as File.</li>
 *         </ul></li>
 *         <li>Intuitive and flexible user interface.
 *         <ul>
 *             <li>Creation/modification of elements follow traditional, ubiquitous procedures. Users
 *             familiar with other applications will feel right at home using EDAmame. Selection, cut,
 *             copy, drag, modification, placement all work as most people would expect. Advanced
 *             features are available but unobtrusive to novice users.</li>
 *             <li>a single unified application window handles all different tasks. No more cluttering
 *             of the desktop with different windows for different tasks/designs.</li>
 *             <li>UI supports advanced control, placement, and alignment of elements with
 *             grids, snaps, shape constraints, user defined coordinate systems, and magnetic elements.</li>
 *         </ul></li>
 *         <li>Platform independence.
 *         <ul>
 *             <li>EDAmame is written in JavaFX.</li>
 *             <li>Runs on all platforms with a JVM. Looks and feels the same on all platforms.</li>
 *             <li>Easy to install. Portable with minimal impact on operating system.</li>
 *             <li>JavaFX/FXML facilitates extension and community development of EDAmame.</li>
 *             <li>Look and feel of EDAmame is easily customized with JavaFX CSS files.</li>
 *         </ul></li>
 *         <li>Automated Bill of Material generation and maintenance.
 *         <ul>
 *             <li>Generic symbols coupled with schematic design constraints and huge part libraries
 *             provides automatic part cross-referencing. Procurement/modification of part manufacturers
 *             does not require changes to symbols, footprints or schematics.</li>
 *             <li>Planned support for vendor APIs. Provides real-time inventory, pricing, quantity
 *             breakpoints.</li>
 *             <li>Designers spend time specifying design requirements, not selecting/changing parts.
 *             Final part choice is done automatically based on design requirement and refining goals
 *             such as minimizing cost, vendor preference, or inventory availability.</li>
 *         </ul></li>
 *     </ul>
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
public class EDAmame
{
    /**
     * The main method that launches EDAmame.<p>
     *
     *     just a veneer to the actual JavaFX Application class due to class loader limitations in Java. This
     *     veneer allows the EDAmame product to be packaged as a runnable JAR file.
     *
     * @param args Command line arguments provided to EDAmame.
     */
    public static void main(String [] args)
    {
        EDAmameApplication.main(args);
    }
}
