module com.kodewerk.gcsee.it {
    requires com.kodewerk.gcsee.api;
    requires com.kodewerk.gcsee.parser;
    requires com.kodewerk.gcsee.vertx;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    uses com.kodewerk.gcsee.aggregator.Aggregation;
    uses com.kodewerk.gcsee.jvm.JavaVirtualMachine;
    uses com.kodewerk.gcsee.jvm.Diarizer;
    uses com.kodewerk.gcsee.message.DataSourceParser;
    uses com.kodewerk.gcsee.message.DataSourceChannel;
    uses com.kodewerk.gcsee.message.DataSourceChannelListener;
    uses com.kodewerk.gcsee.message.JVMEventChannel;
    uses com.kodewerk.gcsee.message.JVMEventChannelListener;
}