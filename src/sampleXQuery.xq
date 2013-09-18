declare namespace xf = "http://tempuri.org/OSSBUnitTesting/sampleXQuery";

declare function xf:GetNewXML($books as element(), $myString, $myInteger, $myDouble, $myBool, $myDate) 
	as element(NewXML) {
	<NewXML>
		{ for $book in $books/Book
			return 
				<BookTitle>
					<Creator>{data($book/Author)}</Creator>
					<YearPublished>{data($book/Year)}</YearPublished>
					<OriginalTitle>{data($book/Title)}</OriginalTitle>
				</BookTitle>
		}
		<SampleString>{$myString}</SampleString>
		<SampleInteger>{$myInteger}</SampleInteger>
		<SampleDouble>{$myDouble}</SampleDouble>
		<SampleBool>{$myBool}</SampleBool>
		<SampleDate>{$myDate}</SampleDate>
	</NewXML>
};

declare variable $books as element() external;
declare variable $myString external;
declare variable $myInteger external;
declare variable $myDouble external;
declare variable $myBool external;
declare variable $myDate external;

xf:GetNewXML($books, $myString, $myInteger, $myDouble, $myBool, $myDate)