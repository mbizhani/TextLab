import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.Tokenizer;
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel;
import com.johnsnowlabs.nlp.annotators.sentence_detector_dl.SentenceDetectorDLModel;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.List;

public class TestSparkNLP {

	public static void main(String[] args) {

		final SparkSession spark = SparkSession
			.builder()
			.appName("SparkNLP")
			.master("local[*]")
			.getOrCreate();

		//PretrainedPipeline.fromDisk("spark/models/recognize_entities_dl_fa_4.0.0_3.0_1656494752817");

		// TIP: https://stackoverflow.com/questions/43633696/dataframe-from-liststring-in-java
		final List<String> sentences = List.of(
			"پس از ظهر، مادر نون و پنیر برای من فرستاد. من به خونه حسن می روم. زندگی زیبا است. علی کلی رانندگی کرده است.",
			"من در ماه می می‌روم"
		);
		final Dataset<Row> data = spark.createDataset(sentences, Encoders.STRING()).toDF("text");
		data.show(false);

		final PerceptronModel posModel = (PerceptronModel) PerceptronModel.load("spark/models/pos_seraji_fa_3.4.3_3.0_1648797830880");
		posModel.setInputCols(new String[]{"sentence", "token"});
		posModel.setOutputCol("pos");

		final Pipeline pipeline = new Pipeline()
			.setStages(new PipelineStage[]{
				documentAssembler("text", "document"),
				sentenceModel("document", "sentence"),
				tokenizer("sentence", "token"),
				posModel,
				finisher("token", "pos")
			});

		final Dataset<Row> transform = pipeline.fit(data).transform(data);
		transform.select("token", "pos").show(false);

		for (int s = 0; s < sentences.size(); s++) {
			final String snt = sentences.get(s);
			System.out.println("snt = " + snt);

			final List<String> token = ((Row[]) transform.select("token").collect())[s].getList(0);
			final List<String> pos = ((Row[]) transform.select("pos").collect())[s].getList(0);

			for (int i = 0; i < token.size() && i < pos.size(); i++) {
				final String t = token.get(i);
				final String p = pos.get(i);

				System.out.printf("\t%s = %s\n", p, t);
			}
		}

		spark.close();
	}

	static DocumentAssembler documentAssembler(String in, String out) {
		final DocumentAssembler documentAssembler = new DocumentAssembler();
		documentAssembler
			.setInputCol(in)
			.setOutputCol(out);
		return documentAssembler;
	}

	static SentenceDetectorDLModel sentenceModel(String in, String out) {
		final SentenceDetectorDLModel sentenceModel = (SentenceDetectorDLModel) SentenceDetectorDLModel.load("spark/models/sentence_detector_dl_xx_2.7.0_2.4_1609610616998");
		sentenceModel.setInputCols(new String[]{in});
		sentenceModel.setOutputCol(out);

		return sentenceModel;
	}

	static List<String> contextChars = Arrays.asList("،", ".", "؟");

	static Tokenizer tokenizer(String in, String out) {
		final Tokenizer tokenizer = new Tokenizer();
		tokenizer.setInputCols(new String[]{in});
		tokenizer.setOutputCol(out);
		contextChars.forEach(tokenizer::addContextChars);

		return tokenizer;
	}

	static Finisher finisher(String... in) {
		return new Finisher()
			.setInputCols(in)
			.setOutputCols(in);
	}
}
